/*
 * Copyright 2013-2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onehippo.repository.documentworkflow.task;

import java.rmi.RemoteException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.WorkflowException;
import org.onehippo.repository.documentworkflow.DocumentVariant;

import static org.hippoecm.repository.api.HippoNodeType.HIPPO_VERSION_HISTORY_PROPERTY;
import static org.hippoecm.repository.api.HippoNodeType.NT_HIPPO_VERSION_INFO;
import static org.onehippo.repository.util.JcrConstants.JCR_VERSION_HISTORY;

/**
 * Custom workflow task for creating a JCR version of a document variant node.
 */
public class VersionVariantTask extends AbstractDocumentTask {

    private static final long serialVersionUID = 1L;

    private DocumentVariant variant;

    public DocumentVariant getVariant() {
        return variant;
    }

    public void setVariant(DocumentVariant variant) {
        this.variant = variant;
    }

    @Override
    public Object doExecute() throws WorkflowException, RepositoryException, RemoteException {

        if (getVariant() == null || !getVariant().hasNode()) {
            throw new WorkflowException("No variant provided");
        }
        final Session workflowSession = getWorkflowContext().getInternalWorkflowSession();
        Node targetNode = getVariant().getNode(workflowSession);

        // ensure no pending changes which would fail the checkin
        workflowSession.save();
        final Node handle = targetNode.getParent();
        if (!handle.isNodeType(NT_HIPPO_VERSION_INFO)) {
            handle.addMixin(NT_HIPPO_VERSION_INFO);
            final String versionHistoryIdentifier = targetNode.getProperty(JCR_VERSION_HISTORY).getNode().getIdentifier();
            handle.setProperty(HIPPO_VERSION_HISTORY_PROPERTY, versionHistoryIdentifier);
            // will be saved later by WorkflowManagerImpl.WorkflowInvocationHandler#invoke
        }
        return new Document(targetNode.getSession().getWorkspace().getVersionManager().checkin(targetNode.getPath()));
    }
}
