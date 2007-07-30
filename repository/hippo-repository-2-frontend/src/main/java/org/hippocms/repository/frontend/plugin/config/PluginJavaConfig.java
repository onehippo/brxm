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
package org.hippocms.repository.frontend.plugin.config;

import java.util.HashMap;
import java.util.Map;

public class PluginJavaConfig implements PluginConfig {
    private static final long serialVersionUID = 1L;
    
    private Map classMap;

    public PluginJavaConfig() {
        classMap = new HashMap();
        classMap.put("navigationPanel", "org.hippocms.repository.plugins.admin.browser.BrowserPlugin");
        classMap.put("menuPanel", "org.hippocms.repository.plugins.admin.menu.MenuPlugin");
        classMap.put("contentPanel", "org.hippocms.repository.plugins.admin.editor.EditorPlugin");
        //classMap.put("contentPanel", "org.hippocms.repository.plugins.samples.SamplesPlugin");
    }

    public String pluginClassname(String id) {
        Object obj = classMap.get(id);
        return (obj == null) ? null : obj.toString();
    }
}
