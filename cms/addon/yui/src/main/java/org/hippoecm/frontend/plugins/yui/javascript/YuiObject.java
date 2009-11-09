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
package org.hippoecm.frontend.plugins.yui.javascript;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.IClusterable;
import org.apache.wicket.util.value.IValueMap;
import org.hippoecm.frontend.plugin.config.IPluginConfig;

public class YuiObject implements IClusterable {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private YuiType type;
    private Map<Setting<?>, Object> settings;
    private List<IYuiListener> listeners;

    public YuiObject(YuiType type) {
        this.type = type;
        listeners = new LinkedList<IYuiListener>();
        settings = new HashMap<Setting<?>, Object>();
        for (Setting<?> setting : type.getProperties()) {
            this.settings.put(setting, setting.newValue());
        }
    }

    public YuiObject(YuiType type, IPluginConfig config) {
        this(type);
        if(config != null) {
            for (Setting<?> setting : settings.keySet()) {
                setting.setFromConfig(config, this);
            }
        }
    }

    public YuiType getType() {
        return type;
    }

    public void updateValues(IValueMap options) {
        for (Setting<?> setting : settings.keySet()) {
            if (options.containsKey(setting.getKey())) {
                // FIXME: remove the fromString(.. toString) construction
                setting.setFromString(options.get(setting.getKey()).toString(), this);
            }
        }
    }

    <T> T get(Setting<T> key) {
        return (T) settings.get(key);
    }

    void set(Setting<?> key, Object value) {
        settings.put(key, value);
        notifyListeners();
    }

    public void addListener(IYuiListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(IYuiListener listener) {
        listeners.remove(listener);
    }
    
    protected void notifyListeners() {
        if (listeners.size() > 0) {
            IYuiListener.Event event = new IYuiListener.Event() {
                @Override
                public YuiObject getSource() {
                    return YuiObject.this;
                }
            };
            for (IYuiListener listener : listeners) {
                listener.onEvent(event);
            }
        }
    }

    public String toScript() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Setting setting : type.getProperties()) {
            Object value = setting.get(this);
            if (setting.isValid(value)) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(setting.getKey() + ": " + setting.getScriptValue(value));
            }
        }
        sb.append('}');
        return sb.toString();
    }

    public boolean isValid() {
        return true;
    }

}
