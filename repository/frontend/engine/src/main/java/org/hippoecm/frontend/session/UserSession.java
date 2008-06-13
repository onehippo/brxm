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
package org.hippoecm.frontend.session;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;

import org.apache.wicket.Application;
import org.apache.wicket.Request;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.util.value.ValueMap;
import org.hippoecm.frontend.Main;
import org.hippoecm.frontend.model.JcrSessionModel;
import org.hippoecm.repository.api.HippoNode;
import org.hippoecm.repository.api.WorkflowManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSession extends WebSession {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(UserSession.class);

    private JcrSessionModel jcrSessionModel;

    public UserSession(Request request, JcrSessionModel jcrSession) {
        super(request);
        jcrSessionModel = jcrSession;
        //Calling the dirty() method causes this wicket session to be reset in the http session
        //so that it knows that the wicket session has changed (we've just added the jcr session model etc.)
        dirty();
    }

    public Session getJcrSession() {
        return jcrSessionModel.getSession();
    }

    public JcrSessionModel getJcrSessionModel() {
        return jcrSessionModel;
    }

    public void setJcrCredentials(ValueMap credentials) {
        jcrSessionModel = new JcrSessionModel(credentials);
    }

    public void logout() {
        jcrSessionModel.logout();
        throw new RestartResponseException(((Main) Application.get()).getHomePage());
    }

    public ValueMap getCredentials() {
        return jcrSessionModel.getCredentials();
    }

    public ClassLoader getClassLoader() {
        return jcrSessionModel.getClassLoader();
    }

    public WorkflowManager getWorkflowManager() {
        return jcrSessionModel.getWorkflowManager();
    }

    public QueryManager getQueryManager() {
        try {
            return jcrSessionModel.getSession().getWorkspace().getQueryManager();
        } catch (RepositoryException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public HippoNode getRootNode() {
        HippoNode result = null;
        try {
            Session jcrSession = jcrSessionModel.getSession();
            if (jcrSession != null) {
                result = (HippoNode) jcrSession.getRootNode();
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        }
        return result;
    }

}
