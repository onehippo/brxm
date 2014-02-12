/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.repository.documentworkflow.action;

import org.apache.commons.scxml2.SCXMLExpressionException;
import org.apache.commons.scxml2.model.ModelException;
import org.onehippo.repository.documentworkflow.WorkflowRequest;
import org.onehippo.repository.documentworkflow.task.RejectRequestTask;

/**
 * RejectRequestAction delegating the execution to RejectRequestTask.
 */
public class RejectRequestAction extends AbstractDocumentTaskAction<RejectRequestTask> {

    private static final long serialVersionUID = 1L;

    public void setRequestExpr(String requestExpr) {
        setParameter("requestExpr", requestExpr);
    }

    public String getRequestExpr() {
        return getParameter("requestExpr");
    }

    public void setReasonExpr(String reasonExpr) {
        setParameter("reasonExpr", reasonExpr);
    }

    public String getReasonExpr() {
        return getParameter("reasonExpr");
    }
    @Override
    protected RejectRequestTask createWorkflowTask() {
        return new RejectRequestTask();
    }

    @Override
    protected void initTask(RejectRequestTask task) throws ModelException, SCXMLExpressionException {
        super.initTask(task);
        task.setRequest((WorkflowRequest) eval(getRequestExpr()));
        task.setReason((String)eval(getReasonExpr()));
    }
}
