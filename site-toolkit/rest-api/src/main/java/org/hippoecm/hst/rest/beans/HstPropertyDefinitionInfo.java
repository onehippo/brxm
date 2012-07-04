/*
 *  Copyright 2012 Hippo.
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

package org.hippoecm.hst.rest.beans;

import java.lang.annotation.Annotation;
import java.util.List;

import org.hippoecm.hst.configuration.channel.HstPropertyDefinition;
import org.hippoecm.hst.core.parameters.HstValueType;

/** 
 * An information class for {@link HstPropertyDefinition}
 */
public class HstPropertyDefinitionInfo {

    private boolean isRequired;
    private Object defaultValue;
    private String name;
    private HstValueType hstValueType;
    private List<? extends Annotation> annotations;

    public HstPropertyDefinitionInfo() {
    }

    // I had to do it this way cause Jackson JSON parser was complaining
    public boolean getIsRequired() {
        return isRequired;
    }

    // I had to do it this way cause Jackson JSON parser was complaining
    public void setIsRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public HstValueType getValueType() {
        return hstValueType;
    }

    public void setValueType(HstValueType hstValueType) {
        this.hstValueType = hstValueType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<? extends Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<? extends Annotation> annotations) {
        this.annotations = annotations;
    }

    public Annotation getAnnotation(Class<? extends Annotation> annotationClass) {
        for (Annotation annotation : getAnnotations()) {
            if (annotation.annotationType().equals(annotationClass)) {
                return annotation;
            }
        }
        return null;
    }

}
