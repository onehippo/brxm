/*
 * Copyright 2007 Hippo
 *
 * Licensed under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.frontend.plugin.editor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.core.Plugin;
import org.hippoecm.frontend.core.PluginContext;
import org.hippoecm.frontend.core.impl.PluginConfig;
import org.hippoecm.frontend.dialog.ExceptionDialog;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.config.ConfigValue;
import org.hippoecm.frontend.plugin.parameters.ParameterValue;
import org.hippoecm.frontend.plugin.render.RenderPlugin;
import org.hippoecm.frontend.service.IDialogService;
import org.hippoecm.frontend.service.IFactoryService;
import org.hippoecm.frontend.service.IRenderService;
import org.hippoecm.frontend.service.IViewService;
import org.hippoecm.frontend.service.render.RenderService;
import org.hippoecm.frontend.util.ServiceTracker;
import org.hippoecm.repository.api.HippoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiEditorPlugin implements Plugin, IViewService, IFactoryService, Serializable {
    private static final long serialVersionUID = 1L;

    public static final Logger log = LoggerFactory.getLogger(MultiEditorPlugin.class);

    public static final String EDITOR_ID = "editor";
    public static final String EDITOR_CLASS = "editor.class";

    private static class PluginEntry implements Serializable {
        private static final long serialVersionUID = 1L;

        String id;
        Plugin plugin;
    }

    private PluginContext context;
    private ServiceTracker<IDialogService> dialogTracker;
    private Map<String, ParameterValue> properties;
    private String editorClass;
    private Map<IModel, PluginEntry> editors;
    private int editCount;

    public MultiEditorPlugin() {
        editors = new HashMap<IModel, PluginEntry>();
        editCount = 0;
        dialogTracker = new ServiceTracker(IDialogService.class);
    }

    public void start(PluginContext context) {
        this.context = context;
        properties = context.getProperties();

        if (properties.get(RenderService.DIALOG_ID) != null) {
            dialogTracker.open(context, properties.get(RenderService.DIALOG_ID).getStrings().get(0));
        } else {
            log.warn("No dialog service ({}) defined", RenderService.DIALOG_ID);
        }

        if (properties.get(EDITOR_CLASS) != null) {
            editorClass = properties.get(EDITOR_CLASS).getStrings().get(0);
            try {
                Class clazz = Class.forName(editorClass);
                if (!IViewService.class.isAssignableFrom(clazz)) {
                    log.error("Specified editor class does not implement IEditService");
                }
                if (!IRenderService.class.isAssignableFrom(clazz)) {
                    log.error("Specified editor class does not implement IRenderService");
                }
            } catch (ClassNotFoundException ex) {
                log.error(ex.getMessage());
            }
        } else {
            log.error("No editor class ({}) defined", EDITOR_CLASS);
        }

        if (properties.get(Plugin.SERVICE_ID) != null) {
            context.registerService(this, properties.get(Plugin.SERVICE_ID).getStrings().get(0));
        } else {
            log.warn("No service id defined");
        }
    }

    public void stop() {
        for (Map.Entry<IModel, PluginEntry> entry : editors.entrySet()) {
            entry.getValue().plugin.stop();
            editors.remove(entry.getKey());
        }
        dialogTracker.close();
    }

    public String getServiceId() {
        if (properties.get(Plugin.SERVICE_ID) != null) {
            return properties.get(Plugin.SERVICE_ID).getStrings().get(0);
        }
        return null;
    }

    public void view(final IModel model) {
        Plugin plugin;
        if (!editors.containsKey(model)) {
            PluginConfig config = new PluginConfig();
            String editorId = properties.get(EDITOR_ID).getStrings().get(0) + editCount;
            config.put(Plugin.SERVICE_ID, new ConfigValue(editorId));
            config.put(Plugin.CLASSNAME, new ConfigValue(editorClass));

            config.put(RenderPlugin.WICKET_ID, properties.get(RenderPlugin.WICKET_ID));
            config.put(RenderPlugin.DIALOG_ID, properties.get(RenderPlugin.DIALOG_ID));

            String factoryId = editorId + ".factory";
            context.registerService(this, factoryId);

            plugin = context.start(config);
            if (plugin instanceof IViewService) {
                ((IViewService) plugin).view(model);
            }

            PluginEntry entry = new PluginEntry();
            entry.plugin = plugin;
            entry.id = editorId;
            editors.put(model, entry);

            editCount++;
        } else {
            plugin = editors.get(model).plugin;
        }
        if (plugin instanceof IRenderService) {
            ((IRenderService) plugin).focus(null);
        }
    }

    public void deleteEditor(IViewService service) {
        Map.Entry<IModel, PluginEntry> entry = getPluginEntry(service);
        if (entry != null) {
            String editorId = entry.getValue().id;
            context.unregisterService(this, editorId + ".factory");

            entry.getValue().plugin.stop();
            editors.remove(entry.getKey());
        } else {
            log.error("unknown editor " + service + " delete is ignored");
        }
    }

    public void delete(Serializable service) {
        IViewService viewer = (IViewService) service;
        Map.Entry<IModel, PluginEntry> entry = getPluginEntry(viewer);
        if (entry != null) {
            IDialogService dialogService = null;
            if (dialogTracker.getServices().size() > 0) {
                dialogService = dialogTracker.getServices().get(0);
            }

            try {
                Node node = ((JcrNodeModel) entry.getKey()).getNode();
                HippoSession session = (HippoSession) node.getSession();
                if (dialogService != null && session.pendingChanges(node, "nt:base").hasNext()) {
                    dialogService.show(new OnCloseDialog(context, dialogService, (JcrNodeModel) entry.getKey(), this,
                            viewer));
                } else {
                    deleteEditor(viewer);
                }
            } catch (RepositoryException e) {
                if (dialogService != null) {
                    dialogService.show(new ExceptionDialog(context, dialogService, e.getMessage()));
                    log.error(e.getClass().getName() + ": " + e.getMessage());
                } else {
                    log.error(e.getMessage());
                }
            }
        }
    }

    private Map.Entry<IModel, PluginEntry> getPluginEntry(IViewService service) {
        for (Map.Entry<IModel, PluginEntry> entry : editors.entrySet()) {
            if (entry.getValue().plugin.equals(service)) {
                return entry;
            }
        }
        return null;
    }
}
