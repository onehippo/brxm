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
package org.hippoecm.frontend.sa.template.plugins;

import org.hippoecm.frontend.model.properties.JcrPropertyValueModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.widgets.DateFieldWidget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatePickerPlugin extends RenderPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(DatePickerPlugin.class);

    private JcrPropertyValueModel valueModel;

    public DatePickerPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        valueModel = (JcrPropertyValueModel) getModel();
        add(new DateFieldWidget("value", valueModel));
        setOutputMarkupId(true);
    }

    @Override
    public void onDetach() {
        if (valueModel == null) {
            log.error("ValueModel is null: " + getModel().toString());
        } else {
            valueModel.detach();
        }
        super.onDetach();
    }
}
