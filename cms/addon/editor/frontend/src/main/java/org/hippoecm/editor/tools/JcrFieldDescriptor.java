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
package org.hippoecm.editor.tools;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.event.EventCollection;
import org.hippoecm.frontend.model.event.IEvent;
import org.hippoecm.frontend.model.event.IObservationContext;
import org.hippoecm.frontend.model.ocm.JcrObject;
import org.hippoecm.frontend.types.IFieldDescriptor;
import org.hippoecm.frontend.types.ITypeDescriptor;
import org.hippoecm.frontend.types.TypeDescriptorEvent;
import org.hippoecm.repository.api.HippoNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcrFieldDescriptor extends JcrObject implements IFieldDescriptor {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id: JcrFieldDescriptor.java 19407 2009-08-26 10:44:37Z fvlankvelt $";

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(JcrFieldDescriptor.class);

    private Set<String> excluded;
    private ITypeDescriptor type;
    private String name;

    public JcrFieldDescriptor(JcrNodeModel model, JcrTypeDescriptor type) {
        super(model);
        this.type = type;
        try {
            this.name = model.getNode().getName();
            if (name.startsWith("hipposysedit:")) {
                log.debug("Property " + HippoNodeType.HIPPO_NAME + " is deprecated, use the node name instead");
                name = getString(HippoNodeType.HIPPO_NAME);
            }
        } catch (RepositoryException e) {
            log.error("Error determining field name", e);
        }
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return getString(HippoNodeType.HIPPO_PATH);
    }

    public void setPath(String path) {
        setString(HippoNodeType.HIPPO_PATH, path);
    }

    public String getType() {
        return getString(HippoNodeType.HIPPOSYSEDIT_TYPE);
    }

    public void setType(String type) {
        setString(HippoNodeType.HIPPOSYSEDIT_TYPE, type);
    }

    public boolean isMandatory() {
        return getBoolean(HippoNodeType.HIPPO_MANDATORY);
    }

    public void setMandatory(boolean mandatory) {
        setBoolean(HippoNodeType.HIPPO_MANDATORY, mandatory);
    }

    public boolean isMultiple() {
        return getBoolean(HippoNodeType.HIPPO_MULTIPLE);
    }

    public void setMultiple(boolean multiple) {
        setBoolean(HippoNodeType.HIPPO_MULTIPLE, multiple);
    }

    public boolean isOrdered() {
        return getBoolean(HippoNodeType.HIPPO_ORDERED);
    }

    public void setOrdered(boolean isOrdered) {
        setBoolean(HippoNodeType.HIPPO_ORDERED, isOrdered);
    }

    public boolean isPrimary() {
        return getBoolean(HippoNodeType.HIPPO_PRIMARY);
    }

    public Set<String> getExcluded() {
        return excluded;
    }

    public void setExcluded(Set<String> set) {
        excluded = set;
    }

    public boolean isAutoCreated() {
        return getBoolean(HippoNodeType.HIPPO_AUTOCREATED);
    }

    public void setAutoCreated(boolean autocreated) {
        setBoolean(HippoNodeType.HIPPO_AUTOCREATED, autocreated);
    }

    public boolean isProtected() {
        return getBoolean(HippoNodeType.HIPPO_PROTECTED);
    }

    public Set<String> getValidators() {
        return Collections.unmodifiableSet(getStringSet(HippoNodeType.HIPPO_VALIDATORS));
    }

    public void addValidator(String validator) {
        Set<String> validators = getStringSet(HippoNodeType.HIPPO_VALIDATORS);
        validators.add(validator);
        setStringSet(HippoNodeType.HIPPO_VALIDATORS, validators);
    }

    public void removeValidator(String validator) {
        Set<String> validators = getStringSet(HippoNodeType.HIPPO_VALIDATORS);
        validators.remove(validator);
        setStringSet(HippoNodeType.HIPPO_VALIDATORS, validators);
    }

    @Override
    protected void processEvents(IObservationContext context, Iterator<? extends IEvent> events) {
        EventCollection<TypeDescriptorEvent> collection = new EventCollection<TypeDescriptorEvent>();
        collection.add(new TypeDescriptorEvent(type, this, TypeDescriptorEvent.EventType.FIELD_CHANGED));
        context.notifyObservers(collection);
    }

    void copy(IFieldDescriptor source) {
        setName(source.getName());
        setType(source.getType());
        setPath(source.getPath());
        setExcluded(source.getExcluded());

        //        setBinary(source.isBinary());
        setMandatory(source.isMandatory());
        setMultiple(source.isMultiple());
        setOrdered(source.isOrdered());
        setPrimary(source.isPrimary());
        //        setProtected(source.isProtected());
    }

    void setName(String name) {
        setString(HippoNodeType.HIPPO_NAME, name);
    }

    void setPrimary(boolean isprimary) {
        setBoolean(HippoNodeType.HIPPO_PRIMARY, isprimary);
    }

    private String getString(String path) {
        try {
            if (getNode().hasProperty(path)) {
                return getNode().getProperty(path).getString();
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
        return null;
    }

    private void setString(String path, String value) {
        try {
            getNode().setProperty(path, value);
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
    }

    private Set<String> getStringSet(String path) {
        Set<String> result = new TreeSet<String>();
        try {
            if (getNode().hasProperty(path)) {
                Value[] values = getNode().getProperty(path).getValues();
                for (int i = 0; i < values.length; i++) {
                    result.add(values[i].getString());
                }
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
        return result;
    }

    private void setStringSet(String path, Set<String> strings) {
        try {
            if (strings != null && strings.size() > 0) {
                ValueFactory vf = getNode().getSession().getValueFactory();
                Value[] values = new Value[strings.size()];
                int i = 0;
                for (Iterator<String> iter = strings.iterator(); iter.hasNext();) {
                    values[i++] = vf.createValue(iter.next());
                }
                getNode().setProperty(path, values);
            } else {
                getNode().setProperty(path, (Value) null);
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
    }

    private boolean getBoolean(String path) {
        try {
            if (getNode().hasProperty(path)) {
                return getNode().getProperty(path).getBoolean();
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage(), ex);
        }
        return false;
    }

    private void setBoolean(String path, boolean value) {
        try {
            getNode().setProperty(path, value);
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }
    }

}
