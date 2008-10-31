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
package org.hippoecm.frontend;

import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.protocol.http.WebResponse;
import org.hippoecm.frontend.dialog.DialogService;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.i18n.IModelProvider;
import org.hippoecm.frontend.i18n.JcrSearchingProvider;
import org.hippoecm.frontend.model.IJcrNodeModelListener;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.JcrSessionModel;
import org.hippoecm.frontend.plugin.IServiceTracker;
import org.hippoecm.frontend.plugin.config.IClusterConfig;
import org.hippoecm.frontend.plugin.config.IPluginConfigService;
import org.hippoecm.frontend.plugin.config.impl.PluginConfigFactory;
import org.hippoecm.frontend.plugin.impl.PluginContext;
import org.hippoecm.frontend.plugin.impl.PluginManager;
import org.hippoecm.frontend.service.IJcrService;
import org.hippoecm.frontend.service.IRenderService;
import org.hippoecm.frontend.service.PluginRequestTarget;
import org.hippoecm.frontend.service.ServiceTracker;
import org.hippoecm.frontend.session.UserSession;

public class Home extends WebPage implements IServiceTracker<IRenderService>, IRenderService, IStringResourceProvider {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private PluginManager mgr;
    private IRenderService root;
    private IPluginConfigService pluginConfigService;
    private IModelProvider<IModel> localizer;

    public Home() {
        add(new EmptyPanel("root"));

        mgr = new PluginManager(this);
        PluginContext context = new PluginContext(mgr, "home");
        context.connect(null);

        context.registerTracker(this, "service.root");

        JcrSessionModel sessionModel = ((UserSession) getSession()).getJcrSessionModel();
        PluginConfigFactory configFactory = new PluginConfigFactory(sessionModel);
        pluginConfigService = configFactory.getPluginConfigService();
        context.registerService(pluginConfigService, IPluginConfigService.class.getName());

        // register JCR service to notify plugins of updates to the jcr tree
        IJcrService jcrService = new IJcrService() {
            private static final long serialVersionUID = 1L;

            public void flush(JcrNodeModel model) {
                List<IJcrNodeModelListener> listeners = mgr.getServices(IJcrService.class.getName(),
                        IJcrNodeModelListener.class);
                for (IJcrNodeModelListener listener : listeners) {
                    listener.onFlush(model);
                }
            }
        };
        context.registerService(jcrService, IJcrService.class.getName());

        DialogService dialogService = new DialogService();
        dialogService.init(context, IDialogService.class.getName(), "dialog");
        add(dialogService);

        context.registerService(this, Home.class.getName());
        String serviceId = context.getReference(this).getServiceId();
        ServiceTracker<IBehavior> tracker = new ServiceTracker<IBehavior>(IBehavior.class) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onServiceAdded(IBehavior behavior, String name) {
                add(behavior);
            }

            @Override
            public void onRemoveService(IBehavior behavior, String name) {
                remove(behavior);
            }
        };
        context.registerTracker(tracker, serviceId);

        IClusterConfig pluginCluster = pluginConfigService.getDefaultCluster();
        context.start(pluginCluster);

        localizer = new JcrSearchingProvider();
    }

    public Component getComponent() {
        return this;
    }

    public void render(PluginRequestTarget target) {
        if (root != null) {
            root.render(target);
        }
    }

    public void focus(IRenderService child) {
    }

    public void bind(IRenderService parent, String wicketId) {
    }

    public void unbind() {
    }

    public IRenderService getParentService() {
        return null;
    }

    public String getServiceId() {
        return null;
    }

    // DO NOT CALL THIS METHOD
    // Use the IPluginContext to access the plugin manager
    public final PluginManager getPluginManager() {
        return mgr;
    }

    @Override
    protected void setHeaders(WebResponse response) {
        response.setHeader("Pragma", "no-cache");
        // FF3 bug: no-store shouldn't be necessary
        response.setHeader("Cache-Control", "no-store, no-cache, max-age=0, must-revalidate"); // no-store
    }

    public void addService(IRenderService service, String name) {
        root = service;
        root.bind(this, "root");
        replace(root.getComponent());
    }

    public void removeService(IRenderService service, String name) {
        replace(new EmptyPanel("root"));
        root.unbind();
        root = null;
    }

    public void updateService(IRenderService service, String name) {
    }

    @Override
    public void onDetach() {
        mgr.detach();
        super.onDetach();
    }

    public String getString(Map<String, String> keys) {
        IModel model = localizer.getModel(keys);
        if (model != null) {
            return (String) model.getObject();
        }
        return null;
    }

}
