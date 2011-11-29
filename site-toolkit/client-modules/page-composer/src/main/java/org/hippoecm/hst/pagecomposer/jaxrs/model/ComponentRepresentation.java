/*
 *  Copyright 2010 Hippo.
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
package org.hippoecm.hst.pagecomposer.jaxrs.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.hippoecm.hst.configuration.components.HstComponentConfiguration;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.container.RequestContextProvider;
import org.hippoecm.hst.core.request.HstRequestContext;

/**
 * @version $Id$
 */
@XmlRootElement(name = "component")
public class ComponentRepresentation {

    private String id;
    private String name;
    private String path;
    private String label;
    private String iconPath;
    private String iconURL;
    private String parentId;

    private String componentClassName;
    private String template;

    private String type;
    private String xtype;

    public ComponentRepresentation represent(HstComponentConfiguration componentConfiguration, Mount mount) {

        id = componentConfiguration.getCanonicalIdentifier();
        name = componentConfiguration.getName();
        path = componentConfiguration.getCanonicalStoredLocation();
        if(componentConfiguration.getParent() != null) {
            parentId = componentConfiguration.getParent().getCanonicalIdentifier();
        }
        componentClassName = componentConfiguration.getComponentClassName();
        template = componentConfiguration.getRenderPath();
        type = componentConfiguration.getComponentType().toString();
        xtype = componentConfiguration.getXType();
        label = componentConfiguration.getLabel();
        iconPath = componentConfiguration.getIconPath();
        // still need to create the iconURL from the iconPath
        if(iconPath != null) {
            HstRequestContext requestContext = RequestContextProvider.get();
            iconURL = requestContext.getHstLinkCreator().create(iconPath, mount, true).toUrlForm(requestContext, true);
        }
        return this;
    }

    protected String getTypeValue() {
        return type;
    }

    public String getComponentClassName() {
        return componentClassName;
    }

    public void setComponentClassName(String componentClassName) {
        this.componentClassName = componentClassName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getXtype() {
        return xtype;
    }

    public void setXtype(String xtype) {
        this.xtype = xtype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }
    
    

}