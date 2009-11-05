/*
 *  Copyright 2008 Hippo.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.hippoecm.frontend.plugins.cms.root;

import java.util.Map;

import org.apache.wicket.protocol.http.WicketURLDecoder;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.IPlugin;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.EditorException;
import org.hippoecm.frontend.service.IBrowseService;
import org.hippoecm.frontend.service.IController;
import org.hippoecm.frontend.service.IEditor;
import org.hippoecm.frontend.service.IEditorManager;
import org.hippoecm.frontend.service.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControllerPlugin implements IPlugin, IController {
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(ControllerPlugin.class);

    private IPluginContext context;
    private IPluginConfig config;

    public ControllerPlugin(IPluginContext context, IPluginConfig config) {
        this.context = context;
        this.config = config;
        context.registerService(this, IController.class.getName());
    }

    public void process(Map parameters) {
        String[] urlPaths = (String[]) parameters.get("path");
        if (urlPaths != null && urlPaths.length > 0) {
            String jcrPath = WicketURLDecoder.PATH_INSTANCE.decode(urlPaths[0]);
            JcrNodeModel nodeModel = new JcrNodeModel(jcrPath);

            IBrowseService browseService = context.getService(config.getString("browser.id", "service.browse"),
                    IBrowseService.class);
            if (browseService != null) {
                browseService.browse(nodeModel);
            } else {
                log.info("Could not find browse service - document " + jcrPath + " will not be selected");
            }

            if (parameters.containsKey("mode")) {
                String[] modeStr = (String[]) parameters.get("mode");
                if (modeStr != null && modeStr.length > 0) {
                    IEditor.Mode mode;
                    if ("edit".equals(modeStr[0])) {
                        mode = IEditor.Mode.EDIT;
                    } else {
                        mode = IEditor.Mode.VIEW;
                    }
                    IEditorManager editorMgr = context.getService(config.getString("editor.id", "service.edit"),
                            IEditorManager.class);
                    if (editorMgr != null) {
                        IEditor editor = editorMgr.getEditor(nodeModel);
                        try {
                            if (editor == null) {
                                editor = editorMgr.openPreview(nodeModel);
                            }
                            editor.setMode(mode);
                        } catch (EditorException e) {
                            log.info("Could not open editor for " + jcrPath);
                        } catch (ServiceException e) {
                            log.info("Could not open preview for " + jcrPath);
                        }
                    }
                }
            }
        }
    }

}
