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
package org.hippoecm.hst.hstconfiguration.components;

import javax.servlet.ServletConfig;

import org.hippoecm.hst.core.component.GenericHstComponent;
import org.hippoecm.hst.core.component.HstComponentException;
import org.hippoecm.hst.core.component.HstRequest;
import org.hippoecm.hst.core.component.HstResponse;
import org.hippoecm.hst.core.request.ComponentConfiguration;

public class GenericResourceServingHstComponent extends GenericHstComponent {
    
    protected String staticResourceServePath = "/staticresource";
    
    public void init(ServletConfig servletConfig, ComponentConfiguration componentConfig) throws HstComponentException {
        super.init(servletConfig, componentConfig);
        
        String param = servletConfig.getInitParameter("staticResourceServePath");
        
        if (param != null) {
            this.staticResourceServePath = param;
        }
    }
    
    public void doBeforeServeResource(HstRequest request, HstResponse response) throws HstComponentException {
        String resourceId = request.getResourceID();
        
        // This example application assumes the resource is a static file
        // if the resourceId starts with "/".
        // If the resourceId does not start with "/",
        // then the container will dispatch the "serveResourcePath" configured in the configuration.
        // If "serverResourcePath" is not configured, then "renderPath" will be used instead.
        
        if (resourceId != null && resourceId.startsWith("/")) {
            response.setServeResourcePath(this.staticResourceServePath);
        }
    }

}
