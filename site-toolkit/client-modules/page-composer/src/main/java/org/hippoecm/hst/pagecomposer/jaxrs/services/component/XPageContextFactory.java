/*
 * Copyright 2020 Bloomreach
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.hippoecm.hst.pagecomposer.jaxrs.services.component;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.hst.pagecomposer.jaxrs.services.PageComposerContextService;
import org.hippoecm.hst.pagecomposer.jaxrs.services.component.state.util.DocumentStateUtils;
import org.hippoecm.hst.pagecomposer.jaxrs.services.component.state.util.ScheduledRequest;
import org.hippoecm.hst.pagecomposer.jaxrs.services.component.state.util.WorkflowRequest;
import org.hippoecm.hst.pagecomposer.jaxrs.services.experiencepage.XPageUtils;
import org.hippoecm.repository.api.HippoSession;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.util.DocumentUtils;
import org.onehippo.repository.branch.BranchConstants;
import org.onehippo.repository.documentworkflow.BranchHandleImpl;
import org.onehippo.repository.documentworkflow.DocumentWorkflow;

import static java.lang.Boolean.TRUE;
import static org.hippoecm.repository.HippoStdNodeType.HIPPOSTD_STATESUMMARY;
import static org.hippoecm.repository.util.JcrUtils.getStringProperty;
import static org.onehippo.repository.branch.BranchConstants.MASTER_BRANCH_ID;

final class XPageContextFactory {

    XPageContext make(final PageComposerContextService contextService) throws RepositoryException, WorkflowException, RemoteException {

        if (!contextService.isExperiencePageRequest()) {
            return null;
        }


        final String experiencePageHandleUUID = contextService.getExperiencePageHandleUUID();
        final HippoSession userSession = (HippoSession) contextService.getRequestContext().getSession();
        final Node handle = userSession.getNodeByIdentifier(experiencePageHandleUUID);
        final String name = DocumentUtils.getDisplayName(handle).orElse(handle.getName());
        final ScheduledRequest scheduledRequest = DocumentStateUtils.getScheduledRequest(handle);
        final List<WorkflowRequest> workflowRequests = DocumentStateUtils.getWorkflowRequests(handle);
        final DocumentWorkflow workflow = XPageUtils.getDocumentWorkflow(userSession, contextService);


        final String selectedBranchId = contextService.getSelectedBranchId();
        final String useXPageDocBranch;
        if (workflow.listBranches().contains(selectedBranchId)) {
            // xpage doc branch exists for currently selectedBranchId (in Channel mgr)
            useXPageDocBranch = selectedBranchId;
        } else {
            // xpage doc branch does not exist for selectedBranchId, use master
            useXPageDocBranch = MASTER_BRANCH_ID;
        }
        final String documentState = getStringProperty(new BranchHandleImpl(useXPageDocBranch, handle).getUnpublished(), HIPPOSTD_STATESUMMARY,
                "unknown").toLowerCase();

        // note the select branch can not exist for 'selectedBranchId' for the document. That is not a problem as the
        // workflow hints just supports that (but gives only few allowed options back which is fine)
        final Map<String, Serializable> hints = workflow.hints(selectedBranchId);

        // the XPageContext is for branchId and documentState uses the actual USED branch (branch or in case missing)
        // master. The available actions *really* uses the hints of the currently viewed channel branch, regardless
        // whether the XPage Doc is branched for that branch.
        final XPageContext xPageContext = new XPageContext().setBranchId(useXPageDocBranch)
                .setXPageId(experiencePageHandleUUID)
                .setXPageName(name)
                .setXPageState(documentState)
                .setScheduledRequest(scheduledRequest)
                .setWorkflowRequests(workflowRequests)
                .setRenameAllowed(TRUE.equals(hints.get("rename")))
                // NOTE: SCXML currently always allows copy so that is why it is also allowed for branched xpages
                .setCopyAllowed(TRUE.equals(hints.get("copy")) && useXPageDocBranch.equals(MASTER_BRANCH_ID))
                .setMoveAllowed(TRUE.equals(hints.get("move")))
                .setDeleteAllowed(TRUE.equals(hints.get("delete")));

        final Map<String, Map<String, Serializable>> requestsHints = (Map<String, Map<String, Serializable>>) hints.get("requests");
        parseRequestsHints(requestsHints, workflowRequests, xPageContext);

        if (!BranchConstants.MASTER_BRANCH_ID.equals(selectedBranchId)) {
            return xPageContext;
        }

        if (hints.containsKey("inUseBy")) {
            xPageContext.setLockedBy((String) hints.get("inUseBy"));
        }

        if (xPageContext.hasBlockingRequest()) {
            return xPageContext;
        }

        final boolean pageIsUnlocked = StringUtils.isBlank(xPageContext.getLockedBy());
        if (hints.containsKey("publishBranch")) {
            xPageContext.setPublishable(TRUE.equals(hints.get("publishBranch")) && pageIsUnlocked);
        } else if (hints.containsKey("requestPublication")) {
            xPageContext.setRequestPublication(TRUE.equals(hints.get("requestPublication")));
        }

        if (hints.containsKey("depublishBranch")) {
            xPageContext.setUnpublishable(TRUE.equals(hints.get("depublishBranch")) && pageIsUnlocked);
        } else if (hints.containsKey("requestDepublication")) {
            xPageContext.setRequestDepublication(TRUE.equals(hints.get("requestDepublication")));
        }

        return xPageContext;
    }

    private static void parseRequestsHints(final Map<String, Map<String, Serializable>> requestsMap,
                                           final List<WorkflowRequest> workflowRequests,
                                           final XPageContext xPageContext) {
        if (requestsMap == null || requestsMap.isEmpty()) {
            return;
        }

        requestsMap.forEach((requestId, actionsMap) -> {
            final Optional<WorkflowRequest> workflowRequest = workflowRequests.stream()
                    .filter(request -> request.getId().equals(requestId))
                    .findFirst();

            if (!workflowRequest.isPresent()) {
                return;
            }

            actionsMap.forEach((action, status) -> {
                if (!TRUE.equals(status)) {
                    return;
                }

                switch (action) {
                    case "cancelRequest":
                        if (workflowRequest.get().getType().equals("rejected")) {
                            xPageContext.setRejectedRequest(true);
                        } else {
                            xPageContext.setCancelRequest(true);
                        }
                        break;
                    case "acceptRequest":
                        xPageContext.setAcceptRequest(true);
                        break;
                    case "rejectRequest":
                        xPageContext.setRejectRequest(true);
                        break;
                }
            });
        });
    }
}
