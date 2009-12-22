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
package org.hippoecm.frontend.plugins.cms.root;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.standardworkflow.EventLoggerWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutPlugin extends RenderPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(LogoutPlugin.class);

    @SuppressWarnings("unused")
    private String username;

    public LogoutPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        UserSession session = (UserSession) getSession();
        username = session.getJcrSession().getUserID();

        add(new Label("username", new PropertyModel(this, "username")));

        add(new AjaxLink("logout-link") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                LogoutPlugin.this.logout();
            }
        });
    }

    protected void logout() {
        UserSession userSession = (UserSession)getSession();
        try {
            Session session = userSession.getJcrSession();
            if (session != null) {
                session.save();
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        try {
            if (userSession.getRootNode().hasNode("hippo:log")) {
                Workflow workflow = ((HippoWorkspace)userSession.getJcrSession().getWorkspace()).getWorkflowManager().getWorkflow("internal", userSession.getRootNode().getNode("hippo:log"));
                if (workflow instanceof EventLoggerWorkflow) {
                    ((EventLoggerWorkflow)workflow).logEvent(userSession.getJcrSession().getUserID(), "Repository", "logout");
                }
            }
        } catch (RemoteException ex) {
        } catch (RepositoryException ex) {
        }
        userSession.logout();
    }
}
