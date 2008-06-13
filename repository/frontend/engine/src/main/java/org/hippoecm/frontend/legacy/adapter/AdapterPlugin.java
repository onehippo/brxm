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
package org.hippoecm.frontend.legacy.adapter;

import org.apache.wicket.model.IModel;
import org.hippoecm.frontend.legacy.adapter.Adapter;
import org.hippoecm.frontend.plugin.IPlugin;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JavaPluginConfig;
import org.hippoecm.frontend.service.IViewService;

public class AdapterPlugin implements IPlugin, IViewService {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private IPluginConfig config;
    private Adapter adapter;
    private String serviceId;

    public AdapterPlugin(IPluginContext context, IPluginConfig config) {
        this.config = config;
        this.serviceId = config.getString("wicket.viewer");

        IPluginConfig rootConfig = new JavaPluginConfig();
        rootConfig.put("legacy.base", config.getString("legacy.base"));
        rootConfig.put("legacy.plugin", config.getString("legacy.plugin"));
        rootConfig.put("wicket.id", config.getString("wicket.id"));
        rootConfig.put("wicket.model", config.getString("wicket.model"));

        adapter = new Adapter();
        adapter.init(context, rootConfig);

        if (serviceId != null) {
            context.registerService(this, serviceId);
        }
    }

    public void view(IModel model) {
        adapter.setModel(model);
    }

}
