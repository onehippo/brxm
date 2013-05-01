/*
 *  Copyright 2013 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cms7.channelmanager.templatecomposer.deviceskins;

import java.io.Serializable;
import java.util.ResourceBundle;

import org.apache.wicket.Session;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version "$Id$"
 */
public class SimpleStyleableDeviceModel implements StyleableDevice, Serializable {

    private static Logger log = LoggerFactory.getLogger(SimpleStyleableDeviceModel.class);

    protected final IPluginConfig config;
    private final String id;
    private final String name;

    public SimpleStyleableDeviceModel(final IPluginConfig config) {
        this.config = config;
        String configName = config.getName();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(DeviceManager.class.getName(), Session.get().getLocale());
        this.id = configName.substring(configName.lastIndexOf('.') + 1);
        this.name = resourceBundle.getString(id);
    }

    public String getStyle() {
        return config.containsKey("style") ? config.getString("style") : null;
    }

    @Override
    public String getWrapStyle() {
        return config.containsKey("wrapstyle") ? config.getString("wrapstyle") : null;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getId() {
        return id;
    }

}
