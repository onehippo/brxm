/*
 *  Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.frontend.plugins.cms.admin.users;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.breadcrumb.panel.BreadCrumbPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugins.cms.admin.widgets.AjaxLinkLabel;

/**
 * Creates a link to the user detail page for a certain {@link User}
 */
public class ViewUserLinkLabel extends AjaxLinkLabel {

    private final BreadCrumbPanel panelToReplace;
    private final IModel<User> userModel;
    private final IPluginContext pluginContext;

    public ViewUserLinkLabel(final String id, final IModel<User> userModel, final BreadCrumbPanel panelToReplace,
                             final IPluginContext pluginContext) {
        super(id, PropertyModel.of(userModel, "username"));
        this.panelToReplace = panelToReplace;
        this.userModel = userModel;
        this.pluginContext = pluginContext;
    }

    @Override
    public void onClick(final AjaxRequestTarget target) {
        panelToReplace.activate((componentId, breadCrumbModel) ->
                new ViewUserPanel(componentId, pluginContext, breadCrumbModel, userModel));
    }


}
