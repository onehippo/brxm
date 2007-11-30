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
package org.hippoecm.repository;

import java.io.File;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.NotSupportedException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.hippoecm.repository.api.UserTransactionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class HippoRepository {

    protected Repository repository;
    protected final Logger log = LoggerFactory.getLogger(HippoRepository.class);
    
    private String JTSLookupName = "java:comp/env/TransactionManager";

    private void initialize() {
    }

    private String workingDirectory;

    protected HippoRepository() {
        workingDirectory = new File(System.getProperty("user.dir")).getAbsolutePath();
        initialize();
    }

    protected HippoRepository(String workingDirectory) {
        if (workingDirectory == null || workingDirectory.equals("")) {
            throw new NullPointerException();
        }
        this.workingDirectory = new File(workingDirectory).getAbsolutePath();
        initialize();
    }

    protected String getWorkingDirectory() {
        return workingDirectory;
    }

    protected String getLocation() {
        return workingDirectory;
    }

    /**
     * Mimic jcr repository login. 
     * @return Session with Anonymous credentials
     * @throws LoginException
     * @throws RepositoryException
     */
    public Session login() throws LoginException, RepositoryException {
        return login(null);
    }

    public Session login(String username, char[] password) throws LoginException, RepositoryException {
        if (username != null && !username.equals("")) {
            // SimpleCredentials give NPE on null as password
            if (password == null) {
                throw new LoginException("Password is null");
            }
            return login(new SimpleCredentials(username, password), null);
        } else {
            return login(null);
        }
    }

    public Session login(SimpleCredentials credentials, String workspaceName) throws LoginException, RepositoryException {
        if (repository == null) {
            throw new RepositoryException("Repository not initialized yet.");
        }

        // try to login with credentials
        Session session = (Session) repository.login(credentials, workspaceName);
        if (session != null) {
            log.info("Logged in as " + session.getUserID() + " to a "
                    + repository.getDescriptor(Repository.REP_NAME_DESC) + " repository.");
        } else if (credentials == null) {
            log.error("Failed to login to repository with no credentials");
        } else {
            log.error("Failed to login to repository with credentials " + credentials.toString());
        }
        return session;
    }
    
    public Session login(SimpleCredentials credentials) throws LoginException, RepositoryException {
        return login(credentials, null);
    }

    public void close() {
        HippoRepositoryFactory.unregister(this);
    }
    
    /**
     * Get a UserTransaction from the JTA transaction manager through JNDI
     * @param session
     * @return a new UserTransactionImpl object
     * @throws RepositoryException
     * @throws NotSupportedException
     */
    public UserTransaction getUserTransaction(Session session) throws RepositoryException, NotSupportedException {
        TransactionManager tm = null;
        InitialContext ic;
        try {
            ic = new InitialContext();
            tm = (TransactionManager)ic.lookup(JTSLookupName);
            log.info("Got TransactionManager through JNDI from " + JTSLookupName);
        } catch (NamingException e) {
            log.error("Failed to get TransactionManager", e);
            throw new RepositoryException("Failed to get TransactionManager.");
        }
        return getUserTransaction(tm, session);
    }

    /**
     * Get a UserTransaction from the JTA transaction manager. 
     * @param tm the (external) transaction manager
     * @param session
     * @return a new UserTransactionImpl object
     * @throws NotSupportedException when Session is not a XASession
     */
    public UserTransaction getUserTransaction(TransactionManager tm, Session session) throws NotSupportedException {
        UserTransaction ut = new UserTransactionImpl(tm, session);
        return ut;
    }
}
