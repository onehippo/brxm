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
package org.hippoecm.frontend.sa.template;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.apache.jackrabbit.value.BooleanValue;
import org.apache.jackrabbit.value.DateValue;
import org.apache.jackrabbit.value.DoubleValue;
import org.apache.jackrabbit.value.LongValue;
import org.apache.jackrabbit.value.NameValue;
import org.apache.jackrabbit.value.PathValue;
import org.apache.jackrabbit.value.ReferenceValue;
import org.apache.jackrabbit.value.StringValue;
import org.apache.wicket.IClusterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeDescriptor implements IClusterable {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(TypeDescriptor.class);

    private String name;
    private String type;
    private List<String> superTypes;
    private Map<String, FieldDescriptor> fields;
    private boolean node;
    private boolean mixin;

    public TypeDescriptor(String name, String type) {
        this.name = name;
        this.type = type;
        this.superTypes = new LinkedList<String>();;
        this.fields = new HashMap<String, FieldDescriptor>();
        this.node = true;
        this.mixin = false;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getSuperTypes() {
        return superTypes;
    }

    public void setSuperTypes(List<String> superTypes) {
        this.superTypes = superTypes;
    }

    public Map<String, FieldDescriptor> getFields() {
        return fields;
    }

    public FieldDescriptor getField(String key) {
        return getFields().get(key);
    }

    public boolean isNode() {
        return node;
    }

    public void setIsNode(boolean isNode) {
        this.node = isNode;
    }

    public boolean isMixin() {
        return mixin;
    }

    public void setIsMixin(boolean isMixin) {
        this.mixin = isMixin;
    }

    public Value createValue() {
        try {
            int propertyType = PropertyType.valueFromName(type);
            switch (propertyType) {
            case PropertyType.BOOLEAN:
                return BooleanValue.valueOf("false");
            case PropertyType.DATE:
                return new DateValue(Calendar.getInstance());
            case PropertyType.DOUBLE:
                return DoubleValue.valueOf("0.0");
            case PropertyType.LONG:
                return LongValue.valueOf("0");
            case PropertyType.NAME:
                return NameValue.valueOf("");
            case PropertyType.PATH:
                return PathValue.valueOf("/");
            case PropertyType.REFERENCE:
                return ReferenceValue.valueOf(UUID.randomUUID().toString());
            case PropertyType.STRING:
            case PropertyType.UNDEFINED:
                return new StringValue("");
            default:
                return null;
            }
        } catch (ValueFormatException ex) {
            log.error(ex.getMessage());
            return null;
        }
    }
}
