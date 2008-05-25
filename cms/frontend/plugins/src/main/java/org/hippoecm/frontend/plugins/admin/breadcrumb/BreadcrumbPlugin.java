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
package org.hippoecm.frontend.plugins.admin.breadcrumb;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.model.IPluginModel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.PluginDescriptor;
import org.hippoecm.frontend.plugin.channel.Notification;

/**
 * A simple plugin displaying the complete JCR path of the current JcrNodeModel.
 * @deprecated Use org.hippoecm.frontend.plugins.console.breadcrumb.BreadcrumbPlugin instead
 */
@Deprecated
public class BreadcrumbPlugin extends Plugin {
    private static final long serialVersionUID = 1L;

    public BreadcrumbPlugin(PluginDescriptor pluginDescriptor, IPluginModel model, Plugin parentPlugin) {
        super(pluginDescriptor, new JcrNodeModel(model), parentPlugin);
        add(new Label("path", new PropertyModel(this, "model.node.path")));
    }

    @Override
    public void receive(Notification notification) {
        if ("select".equals(notification.getOperation())) {
            setPluginModel(new JcrNodeModel(notification.getModel()));
            notification.getContext().addRefresh(this);
        }
        super.receive(notification);
    }
}
