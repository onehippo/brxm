/*
 *  Copyright 2016-2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cm.model.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang3.StringUtils;
import org.onehippo.cm.model.ConfigurationItemCategory;
import org.onehippo.cm.model.DefinitionNode;
import org.onehippo.cm.model.ModelItem;
import org.onehippo.cm.model.PropertyType;
import org.onehippo.cm.model.ValueType;

import com.google.common.collect.Maps;

import static org.onehippo.cm.model.util.SnsUtils.createIndexedName;

public class DefinitionNodeImpl extends DefinitionItemImpl implements DefinitionNode {

    private ConfigurationItemCategory category;
    private final LinkedHashMap<String, DefinitionNodeImpl> modifiableNodes = new LinkedHashMap<>();
    private final Map<String, DefinitionNodeImpl> nodes = Collections.unmodifiableMap(modifiableNodes);
    private final Map<String, DefinitionPropertyImpl> modifiableProperties = new LinkedHashMap<>();
    private final Map<String, DefinitionPropertyImpl> properties = Collections.unmodifiableMap(modifiableProperties);
    private boolean delete = false;
    private String orderBefore = null;
    private Boolean ignoreReorderedChildren;
    private ConfigurationItemCategory residualChildNodeCategory = null;

    public DefinitionNodeImpl(final String path, final String name, final ContentDefinitionImpl definition) {
        super(path, name, definition);
    }

    public DefinitionNodeImpl(final String path, final ContentDefinitionImpl definition) {
        super(path, definition);
    }

    public DefinitionNodeImpl(final String name, final DefinitionNodeImpl parent) {
        super(name, parent);
    }

    @Override
    public ConfigurationItemCategory getCategory() {
        return category;
    }

    public void setCategory(final ConfigurationItemCategory category) {
        this.category = category;
    }

    @Override
    public Map<String, DefinitionNodeImpl> getNodes() {
        return nodes;
    }

    @Override
    public DefinitionNodeImpl getNode(final String name) {
        return modifiableNodes.get(name);
    }

    /**
     * Get a descendant node by its path relative to this node.
     * This is conceptually equivalent to JCR's {@link Node#getNode(String)} method.
     * @param relativePath a slash-separated relative path to a descendant node
     * @return the node at the given relative path
     */
    public DefinitionNodeImpl resolveNode(final String relativePath) {
        final String[] segments = StringUtils.stripStart(relativePath, "/").split("/");

        DefinitionNodeImpl currentNode = this;
        for (String segment : segments) {
            currentNode = currentNode.getNode(segment);
            if (currentNode == null) {
                return null;
            }
        }
        return currentNode;
    }

    public LinkedHashMap<String, DefinitionNodeImpl> getModifiableNodes() {
        return modifiableNodes;
    }

    @Override
    public Map<String, DefinitionPropertyImpl> getProperties() {
        return properties;
    }

    @Override
    public DefinitionPropertyImpl getProperty(final String name) {
        return modifiableProperties.get(name);
    }

    public Map<String, DefinitionPropertyImpl> getModifiableProperties() {
        return modifiableProperties;
    }

    @Override
    public boolean isDelete() {
        return delete;
    }

    public void setDelete(final boolean delete) {
        this.delete = delete;
    }

    @Override
    public String getOrderBefore() {
        return orderBefore;
    }

    public DefinitionNodeImpl setOrderBefore(final String orderBefore) {
        this.orderBefore = orderBefore;
        return this;
    }

    @Override
    public Boolean getIgnoreReorderedChildren() {
        return ignoreReorderedChildren;
    }

    public void setIgnoreReorderedChildren(final Boolean ignoreReorderedChildren) {
        this.ignoreReorderedChildren = ignoreReorderedChildren;
    }

    public DefinitionNodeImpl addNode(final String name) {
        final DefinitionNodeImpl node = new DefinitionNodeImpl(name, this);
        modifiableNodes.put(name, node);
        return node;
    }

    /**
     * Insert a new node before the one with the name given by beforeThis.
     * @param name
     * @param beforeThis
     * @return
     */
    public DefinitionNodeImpl addNodeBefore(final String name, final String beforeThis) {
        // we need to perform an O(n)*2 insert by clearing and rebuilding the nodes map

        // handle case where order-before mentions a node that isn't actually listed here yet
        if (!modifiableNodes.containsKey(beforeThis)) {
            // add at end as usual, and also set the order-before here as a flag to the caller about what happened
            return addNode(name).setOrderBefore(beforeThis);
        }

        // copy the existing child nodes, inserting at the right place
        LinkedHashMap<String, DefinitionNodeImpl> newView = new LinkedHashMap<>();
        final String indexedBeforeThis = createIndexedName(beforeThis);
        DefinitionNodeImpl node = null;
        for (Map.Entry<String, DefinitionNodeImpl> entry : modifiableNodes.entrySet()) {
            if (createIndexedName(entry.getKey()).equals(indexedBeforeThis)) {
                node = new DefinitionNodeImpl(name, this);
                newView.put(name, node);
            }
            newView.put(entry.getKey(), entry.getValue());
        }

        // clear and copy back into the existing child nodes map
        modifiableNodes.clear();
        modifiableNodes.putAll(newView);

        // it should be impossible for this to be null here
        return node;
    }

    /**
     * Reorder existing child nodes of this parent node.
     * @param childNode the child that should be before the one specified by beforeThis
     * @param beforeThis the child that should be after childNode
     * @throws IllegalArgumentException if either of the parameters is not a child of this node
     */
    public void orderBefore(final DefinitionNodeImpl childNode, final DefinitionNodeImpl beforeThis) {
        if (childNode.getParent() != this || beforeThis.getParent() != this) {
            throw new IllegalArgumentException("Cannot reorder nodes that do not belong to this parent!");
        }

        // copy the existing child nodes, inserting at the right place
        LinkedHashMap<String, DefinitionNodeImpl> newView = new LinkedHashMap<>();
        for (Map.Entry<String, DefinitionNodeImpl> entry : modifiableNodes.entrySet()) {
            if (entry.getValue() == beforeThis) {
                newView.put(name, childNode);
            }
            newView.put(entry.getKey(), entry.getValue());
        }

        // clear and copy back into the existing child nodes map
        modifiableNodes.clear();
        modifiableNodes.putAll(newView);
    }

    public DefinitionPropertyImpl addProperty(final String name, final ValueImpl value) {
        final DefinitionPropertyImpl property = new DefinitionPropertyImpl(name, value, this);
        modifiableProperties.put(name, property);
        return property;
    }

    public DefinitionPropertyImpl addProperty(final String name, final ValueType type, final ValueImpl[] values) {
        final DefinitionPropertyImpl property = new DefinitionPropertyImpl(name, type, values, this);
        modifiableProperties.put(name, property);
        return property;
    }

    /**
     * Add a new property by copying the contents of an existing property.
     * @param other the property to copy
     * @return the new property
     */
    public DefinitionPropertyImpl addProperty(final DefinitionPropertyImpl other) {
        if (other.getType() == PropertyType.SINGLE) {
            DefinitionPropertyImpl newProp = addProperty(other.getName(), other.getValue().clone());
            newProp.setOperation(other.getOperation());
            return newProp;
        }
        else {
            DefinitionPropertyImpl newProp =
                    addProperty(other.getName(), other.getValueType(), other.cloneValues(null));
            newProp.setOperation(other.getOperation());
            return newProp;
        }
    }

    public void delete() {
        delete = true;
        modifiableNodes.clear();
        modifiableProperties.clear();
        orderBefore = null;
    }

    public boolean isEmpty() {
        return modifiableNodes.isEmpty() && modifiableProperties.isEmpty() && orderBefore == null;
    }

    public boolean isDeleted() {
        return isDelete() && isEmpty();
    }

    @Override
    public ConfigurationItemCategory getResidualChildNodeCategory() {
        return residualChildNodeCategory;
    }

    public void setResidualChildNodeCategory(final ConfigurationItemCategory category) {
        residualChildNodeCategory = category;
    }

    public void sortProperties() {
        final SortedSet<DefinitionPropertyImpl> properties = new TreeSet<>(modifiableProperties.values());
        modifiableProperties.clear();
        modifiableProperties.putAll(Maps.uniqueIndex(properties, ModelItem::getName));
    }

    public void recursiveSortProperties() {
        sortProperties();
        getNodes().values().forEach(DefinitionNodeImpl::recursiveSortProperties);
    }

    public void visitResources(ValueConsumer consumer) throws RepositoryException, IOException {
        // find resource values
        for (DefinitionPropertyImpl dp : getProperties().values()) {
            switch (dp.getType()) {
                case SINGLE:
                    final ValueImpl val = dp.getValue();
                    if (val.isResource()) {
                        consumer.accept(val);
                    }
                    break;
                case SET:
                case LIST:
                    for (ValueImpl value : dp.getValues()) {
                        if (value.isResource()) {
                            consumer.accept(value);
                        }
                    }
                    break;
            }
        }

        // recursively visit child definition nodes
        for (DefinitionNodeImpl dn : getNodes().values()) {
            dn.visitResources(consumer);
        }
    }

    public interface ValueConsumer {
        void accept(ValueImpl value) throws RepositoryException, IOException;
    }

}
