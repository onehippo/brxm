/*
 *  Copyright 2010 Hippo.
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
package org.hippoecm.frontend.editor.plugins.field;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.hippoecm.frontend.editor.TemplateEngineException;
import org.hippoecm.frontend.editor.compare.IComparer;
import org.hippoecm.frontend.editor.compare.LCS;
import org.hippoecm.frontend.model.AbstractProvider;
import org.hippoecm.frontend.model.ModelReference;
import org.hippoecm.frontend.plugin.IClusterControl;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JavaPluginConfig;
import org.hippoecm.frontend.service.IEditor;
import org.hippoecm.frontend.service.IRenderService;
import org.hippoecm.frontend.service.ServiceContext;
import org.hippoecm.frontend.service.render.RenderService;
import org.hippoecm.frontend.types.ITypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComparingController<C extends IModel> implements IDetachable {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(TemplateController.class);

    public enum Orientation {
        HORIZONTAL, VERTICAL;
    };

    static IPluginConfig wrapConfig(IPluginConfig config, int cnt) {
        JavaPluginConfig wrapped = new JavaPluginConfig(config);
        wrapped.put("cmp", config.get("cmp") + "." + cnt);
        wrapped.put("old", config.get("old") + "." + cnt);
        wrapped.put("new", config.get("new") + "." + cnt);
        return wrapped;
    }

    class ItemEntry extends RenderService {
        private static final long serialVersionUID = 1L;

        C oldModel;
        FieldItem<C> oldFir;

        C newModel;
        FieldItem<C> newFir;

        IClusterControl cmpTpl;

        ItemEntry(ServiceContext sc, C model, int id) throws TemplateEngineException {
            super(sc, wrapConfig(itemConfig, id));

            oldModel = model;

            IPluginConfig config = getPluginConfig();

            cmpTpl = factory.newTemplate(config.getString("cmp"), IEditor.Mode.VIEW);
            addExtensionPoint("cmp");

            String oldModelId = cmpTpl.getClusterConfig().getString(RenderService.MODEL_ID);
            ModelReference oldModelRef = new ModelReference(oldModelId, model);
            oldModelRef.init(sc);

            cmpTpl.start();
        }

        ItemEntry(ServiceContext sc, C oldModel, C newModel, int id) throws TemplateEngineException {
            super(sc, wrapConfig(itemConfig, id));

            this.oldModel = oldModel;
            this.newModel = newModel;

            IPluginConfig config = getPluginConfig();

            if (useCompareWhenPossible && oldModel != null && newModel != null) {
                cmpTpl = factory.newTemplate(config.getString("cmp"), IEditor.Mode.COMPARE);
                if (!cmpTpl.getClusterConfig().getReferences().contains("model.compareTo")) {
                    cmpTpl = null;
                }
            }

            if (cmpTpl != null) {
                addExtensionPoint("cmp");

                String oldModelId = cmpTpl.getClusterConfig().getString("model.compareTo");
                ModelReference oldModelRef = new ModelReference(oldModelId, oldModel);
                oldModelRef.init(sc);

                String newModelId = cmpTpl.getClusterConfig().getString(RenderService.MODEL_ID);
                ModelReference newModelRef = new ModelReference(newModelId, newModel);
                newModelRef.init(sc);

                cmpTpl.start();

            } else if (oldModel != null && newModel != null) {
                addExtensionPoint("old");
                addExtensionPoint("new");

                IClusterControl oldTemplate = factory.newTemplate(config.getString("old"), IEditor.Mode.VIEW);
                oldFir = new FieldItem<C>(sc, oldModel, null, oldTemplate, null);

                IClusterControl newTemplate = factory.newTemplate(config.getString("new"), IEditor.Mode.VIEW);
                newFir = new FieldItem<C>(sc, newModel, null, newTemplate, null);

                get("old").add(new AttributeAppender("class", new Model("hippo-diff-removed"), " "));
                get("new").add(new AttributeAppender("class", new Model("hippo-diff-added"), " "));

            } else {
                addExtensionPoint("cmp");

                cmpTpl = factory.newTemplate(config.getString("cmp"), IEditor.Mode.VIEW);

                String modelId = cmpTpl.getClusterConfig().getString(RenderService.MODEL_ID);
                ModelReference modelRef;
                String cssClass;
                if (oldModel != null) {
                    cssClass = "hippo-diff-removed";
                    modelRef = new ModelReference(modelId, oldModel);
                } else {
                    cssClass = "hippo-diff-added";
                    modelRef = new ModelReference(modelId, newModel);
                }
                modelRef.init(sc);

                cmpTpl.start();

                Component component = get("cmp");
                component.add(new AttributeAppender("class", new Model(cssClass), " "));
            }
        }

        @Override
        public String getVariation() {
            if (oldFir != null || newFir != null) {
                if (orientation == Orientation.HORIZONTAL) {
                    return "leftright";
                } else {
                    return "compat";
                }
            }
            return super.getVariation();
        }

        void destroy() {
            if (oldFir != null) {
                oldFir.destroy();
            }
            if (newFir != null) {
                newFir.destroy();
            }
            if (cmpTpl != null) {
                cmpTpl.stop();
            }
            ((ServiceContext) getPluginContext()).stop();
        }

        @Override
        public void onDetach() {
            if (oldFir != null) {
                oldFir.detach();
            }
            if (newFir != null) {
                newFir.detach();
            }
            super.onDetach();
        }
    }

    static class ItemValue<T extends IModel> {

        private IComparer comparer;
        private T value;
        private int hash;

        ItemValue(IComparer comparer, T value) {
            this.value = value;
            this.comparer = comparer;
            this.hash = comparer.getHashCode(value.getObject());
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ItemValue<?>) {
                ItemValue<?> that = (ItemValue<?>) obj;
                if (hash == that.hash) {
                    return comparer.areEqual(value.getObject(), ((ItemValue<?>) obj).value.getObject());
                }
            }
            return false;
        }

    }

    private IPluginContext context;
    @SuppressWarnings("unused")
    private IPluginConfig config;
    private ITemplateFactory<C> factory;
    private Set<ItemEntry> childTemplates;
    private JavaPluginConfig itemConfig;
    private IComparer comparer;
    private boolean useCompareWhenPossible = true;
    private Orientation orientation = Orientation.VERTICAL;

    public ComparingController(IPluginContext context, IPluginConfig config, ITemplateFactory<C> factory,
            IComparer comparer, String itemId) {
        this.context = context;
        this.config = config;
        this.factory = factory;
        this.comparer = comparer;

        this.itemConfig = new JavaPluginConfig(itemId);
        itemConfig.put("wicket.id", itemId);
        itemConfig.put("cmp", itemId + ".cmp");
        itemConfig.put("old", itemId + ".old");
        itemConfig.put("new", itemId + ".new");

        childTemplates = new HashSet<ItemEntry>();
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setUseCompareWhenPossible(boolean useCompareWhenPossible) {
        this.useCompareWhenPossible = useCompareWhenPossible;
    }

    public boolean isUseCompareWhenPossible() {
        return useCompareWhenPossible;
    }

    public void start(AbstractProvider<C> oldProvider, AbstractProvider<C> newProvider, ITypeDescriptor type) {
        List<ItemValue<C>> oldItems = new LinkedList<ItemValue<C>>();
        Iterator<C> oldIter = oldProvider.iterator(0, oldProvider.size());
        while (oldIter.hasNext()) {
            oldItems.add(new ItemValue<C>(comparer, oldIter.next()));
        }

        List<ItemValue<C>> newItems = new LinkedList<ItemValue<C>>();
        Iterator<C> newIter = newProvider.iterator(0, newProvider.size());
        while (newIter.hasNext()) {
            newItems.add(new ItemValue<C>(comparer, newIter.next()));
        }

        List<ItemValue> common = LCS.getLongestCommonSubsequence(oldItems.toArray(new ItemValue[oldItems.size()]),
                newItems.toArray(new ItemValue[newItems.size()]));

        Iterator<ItemValue> commonIter = common.iterator();
        Iterator<ItemValue<C>> oldValueIter = oldItems.iterator();
        Iterator<ItemValue<C>> newValueIter = newItems.iterator();
        ItemValue<C> nextNewValue = null;
        if (newValueIter.hasNext()) {
            nextNewValue = newValueIter.next();
        }
        int cnt = 0;
        while (commonIter.hasNext()) {
            ItemValue nextValue = commonIter.next();
            while (oldValueIter.hasNext()) {
                ItemValue<C> oldValue = oldValueIter.next();
                if (oldValue.equals(nextValue)) {
                    break;
                } else {
                    if (useCompareWhenPossible && nextNewValue != null && !nextNewValue.equals(nextValue)) {
                        addModelComparison(oldValue.value, nextNewValue.value, cnt++);
                        if (newValueIter.hasNext()) {
                            nextNewValue = newValueIter.next();
                        } else {
                            nextNewValue = null;
                        }
                    } else {
                        addModelComparison(oldValue.value, null, cnt++);
                    }
                }
            }
            while (nextNewValue != null && !nextNewValue.equals(nextValue)) {
                addModelComparison(null, nextNewValue.value, cnt++);
                if (newValueIter.hasNext()) {
                    nextNewValue = newValueIter.next();
                } else {
                    nextNewValue = null;
                }
            }
            addModelView((C) nextValue.value, cnt++);
            nextNewValue = null;
            if (newValueIter.hasNext()) {
                nextNewValue = newValueIter.next();
            }
        }
        while (oldValueIter.hasNext()) {
            ItemValue<C> oldValue = oldValueIter.next();
            if (useCompareWhenPossible && nextNewValue != null) {
                addModelComparison(oldValue.value, nextNewValue.value, cnt++);
                if (newValueIter.hasNext()) {
                    nextNewValue = newValueIter.next();
                } else {
                    nextNewValue = null;
                }
            } else {
                addModelComparison(oldValue.value, null, cnt++);
            }
        }
        while (nextNewValue != null) {
            addModelComparison(null, nextNewValue.value, cnt++);
            if (newValueIter.hasNext()) {
                nextNewValue = newValueIter.next();
            } else {
                nextNewValue = null;
            }
        }
    }

    public void stop() {
        for (ItemEntry entry : childTemplates) {
            entry.destroy();
        }
        childTemplates.clear();
    }

    public ItemEntry getFieldItem(IRenderService renderer) {
        return (ItemEntry) renderer;
    }

    private void addModelView(C model, int id) {
        try {
            ServiceContext serviceContext = new ServiceContext(context);
            childTemplates.add(new ItemEntry(serviceContext, model, id));
        } catch (TemplateEngineException ex) {
            log.error("Failed to open editor for new model", ex);
        }
    }

    private void addModelComparison(C oldModel, C newModel, int id) {
        try {
            ServiceContext serviceContext = new ServiceContext(context);
            childTemplates.add(new ItemEntry(serviceContext, oldModel, newModel, id));
        } catch (TemplateEngineException ex) {
            log.error("Failed to open editor for new model", ex);
        }
    }

    public void detach() {
        for (ItemEntry entry : childTemplates) {
            entry.detach();
        }
    }

}
