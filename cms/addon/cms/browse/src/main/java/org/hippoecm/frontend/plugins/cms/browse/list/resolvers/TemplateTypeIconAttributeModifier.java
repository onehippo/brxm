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
package org.hippoecm.frontend.plugins.cms.browse.list.resolvers;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IDetachable;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.standards.list.resolvers.AbstractNodeAttributeModifier;
import org.hippoecm.frontend.plugins.standards.list.resolvers.CssClassAppender;
import org.hippoecm.repository.api.HippoNodeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateTypeIconAttributeModifier extends AbstractNodeAttributeModifier {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(TemplateTypeIconAttributeModifier.class);

    private static class StateIconAttributes implements IDetachable {
        private static final long serialVersionUID = 1L;

        private JcrNodeModel nodeModel;
        private transient String cssClass;
        private transient boolean loaded = false;

        StateIconAttributes(JcrNodeModel nodeModel) {
            this.nodeModel = nodeModel;
        }

        @SuppressWarnings("unused")
        public String getCssClass() {
            load();
            return cssClass;
        }

        public void detach() {
            loaded = false;
            cssClass = null;
            nodeModel.detach();
        }

        void load() {
            if (!loaded) {
                cssClass = "document-16";
                try {
                    Node node = nodeModel.getNode();
                    if (node != null && node.isNodeType(HippoNodeType.NT_TEMPLATETYPE)) {
                        String prefix = node.getParent().getName();
                        NamespaceRegistry nsReg = node.getSession().getWorkspace().getNamespaceRegistry();
                        String currentUri = nsReg.getURI(prefix);

                        Node ntHandle = node.getNode(HippoNodeType.HIPPOSYSEDIT_NODETYPE);
                        NodeIterator variants = ntHandle.getNodes(HippoNodeType.HIPPOSYSEDIT_NODETYPE);

                        Node current = null;
                        Node draft = null;
                        while (variants.hasNext()) {
                            Node variant = variants.nextNode();
                            if (variant.isNodeType(HippoNodeType.NT_REMODEL)) {
                                String uri = variant.getProperty(HippoNodeType.HIPPO_URI).getString();
                                if (currentUri.equals(uri)) {
                                    current = variant;
                                }
                            } else {
                                draft = variant;
                            }
                        }

                        if (current == null && draft != null) {
                            cssClass = "state-new-16";
                        } else if (current != null && draft == null) {
                            cssClass = "state-live-16";
                        } else if (current != null && draft != null) {
                            cssClass = "state-changed-16";
                        }
                    }
                } catch (RepositoryException ex) {
                    log.error("Unable to obtain state properties", ex);
                }
                loaded = true;
            }
        }
    }

    @Override
    public AttributeModifier getColumnAttributeModifier(Node node) {
        return new CssClassAppender(new Model("icon-16"));
    }

    @Override
    public AttributeModifier[] getCellAttributeModifiers(Node node) {
        StateIconAttributes attrs = new StateIconAttributes(new JcrNodeModel(node));
        AttributeModifier[] attributes = new AttributeModifier[1];
        attributes[0] = new CssClassAppender(new PropertyModel(attrs, "cssClass"));
        return attributes;
    }
}
