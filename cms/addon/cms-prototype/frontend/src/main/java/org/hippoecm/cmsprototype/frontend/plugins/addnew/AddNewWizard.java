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
package org.hippoecm.cmsprototype.frontend.plugins.addnew;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;
import org.hippoecm.frontend.Main;
import org.hippoecm.frontend.model.IPluginModel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.PluginDescriptor;
import org.hippoecm.frontend.plugin.channel.Channel;
import org.hippoecm.frontend.plugin.channel.MessageContext;
import org.hippoecm.frontend.plugin.channel.Request;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.HippoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Form to create new documents.
 * It gets the available document templates from the configuration in the repository.
 *
 */
public class AddNewWizard extends Plugin {
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(AddNewWizard.class);

    private String prototypePath;

    public AddNewWizard(PluginDescriptor pluginDescriptor, IPluginModel model, Plugin parentPlugin) {
        super(pluginDescriptor, new JcrNodeModel(model), parentPlugin);

        Main main = (Main) Application.get();
        prototypePath = HippoNodeType.CONFIGURATION_PATH + "/" + HippoNodeType.FRONTEND_PATH + "/"
                + main.getHippoApplication() + "/" + HippoNodeType.PROTOTYPES_PATH;

        final AddNewForm form = new AddNewForm("addNewForm");
        add(new FeedbackPanel("feedback"));
        add(form);
    }

    private final class AddNewForm extends Form {
        private static final long serialVersionUID = 1L;

        ValueMap properties;

        public AddNewForm(String id) {
            super(id);
            properties = new ValueMap();
            TextField name = new TextField("name", new PropertyModel(properties, "name"));
            name.setRequired(true);
            add(name);

            List<String> prototypes = getPrototypes();
            DropDownChoice prototype = new DropDownChoice("prototype", new PropertyModel(properties, "prototype"),
                    prototypes);
            prototype.setRequired(true);
            add(prototype);

            add(new AjaxButton("submit", this) {
                private static final long serialVersionUID = 1L;

                @Override
                protected void onSubmit(AjaxRequestTarget target, Form form) {

                    Node doc = createDocument();

                    if (doc != null && target != null) {
                        Channel channel = getTopChannel();
                        if (channel != null) {

                            JcrNodeModel model = new JcrNodeModel(doc);
                            Request request = channel.createRequest("flush", model.findRootModel());
                            MessageContext context = request.getContext();
                            channel.send(request);

                            request = channel.createRequest("browse", model);
                            request.setContext(context);
                            channel.send(request);

                            context.apply(target);
                        }
                    }

                    properties.clear();

                }

                @Override
                protected void onError(AjaxRequestTarget target, Form form) {
                    super.onError(target, form);
                    System.out.println("onError!");
                    target.addComponent(AddNewWizard.this);
                }

            });

        }

        private List<String> getPrototypes() {
            List<String> prototypes = new ArrayList<String>();
            UserSession session = (UserSession) Session.get();

            try {
                QueryManager queryManager = session.getJcrSession().getWorkspace().getQueryManager();
                NodeTypeManager ntMgr = session.getJcrSession().getWorkspace().getNodeTypeManager();

                String xpath = prototypePath + "/*";

                Query query = queryManager.createQuery(xpath, Query.XPATH);
                QueryResult result = query.execute();
                NodeIterator iterator = result.getNodes();
                while (iterator.hasNext()) {
                    Node node = iterator.nextNode();
                    try {
                        // name of the node should correspond to a registered node type
                        ntMgr.getNodeType(node.getName());
                        prototypes.add(node.getName());
                    } catch (NoSuchNodeTypeException ex) {
                        log.warn("Template " + node.getName() + " does not correspond to a node type");
                    }
                }
            } catch (RepositoryException e) {
                log.error(e.getMessage());
            }

            return prototypes;
        }

        Node createDocument() {
            UserSession session = (UserSession) Session.get();
            Node result = null;
            String name = (String) properties.get("name");
            String type = (String) properties.get("prototype");

            try {
                Node handle = session.getRootNode().addNode(name, HippoNodeType.NT_HANDLE);

                // find prototype node; use the first node under the prototype handle
                Node prototype = (Node) session.getJcrSession().getItem("/" + prototypePath + "/" + type + "/" + type);
                result = ((HippoSession) session.getJcrSession()).copy(prototype, handle.getPath() + "/" + name);
                handle.save();

            } catch (RepositoryException e) {
                log.error(e.getMessage());
            }

            return result;
        }

    }

}
