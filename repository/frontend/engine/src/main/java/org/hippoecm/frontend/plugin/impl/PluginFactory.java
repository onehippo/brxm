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
package org.hippoecm.frontend.plugin.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.wicket.IClusterable;
import org.apache.wicket.Session;
import org.hippoecm.frontend.plugin.IPlugin;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.config.impl.JavaPluginConfig;
import org.hippoecm.frontend.plugin.error.ErrorPlugin;
import org.hippoecm.frontend.service.render.RenderService;
import org.hippoecm.frontend.session.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginFactory implements IClusterable {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(PluginFactory.class);

    public PluginFactory() {
    }

    public IPlugin createPlugin(IPluginContext context, IPluginConfig config) {
        String className = config.getString(IPlugin.CLASSNAME);
        IPlugin plugin = null;
        String message = null;
        if (className == null) {
            message = "No plugin classname configured, please set plugin configuration parameter " + IPlugin.CLASSNAME;
        } else {
            try {
                ClassLoader loader = ((UserSession) Session.get()).getClassLoader();
                if (loader == null) {
                    log.warn("Unable to retrieve repository classloader, falling back to default classloader.");
                    loader = getClass().getClassLoader();
                }
                Class clazz = Class.forName(className, true, loader);
                Class[] formalArgs = new Class[] { IPluginContext.class, IPluginConfig.class };
                Constructor constructor = clazz.getConstructor(formalArgs);
                Object[] actualArgs = new Object[] { context, config };
                plugin = (IPlugin) constructor.newInstance(actualArgs);
            } catch (InvocationTargetException e) {
                message = e.getTargetException().getClass().getName() + ": "
                        + e.getTargetException().getMessage();
                log.error(e.getMessage());
            } catch (Exception e) {
                message = e.getClass().getName() + ": " + e.getMessage();
                log.error(e.getMessage());
            }
        }
        if (plugin == null && message != null) {
            message +=  "\nFailed to instantiate plugin '" + className +
            "' for id '" + config.getString(RenderService.WICKET_ID) + "'.";

            IPluginConfig errorConfig = new JavaPluginConfig();
            errorConfig.put(ErrorPlugin.ERROR_MESSAGE, message);
            errorConfig.put(RenderService.WICKET_ID, config.getString(RenderService.WICKET_ID));
            plugin = new ErrorPlugin(context, errorConfig);
        }
        return plugin;
    }
}
