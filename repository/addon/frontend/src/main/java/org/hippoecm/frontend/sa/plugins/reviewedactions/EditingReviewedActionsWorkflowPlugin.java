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
package org.hippoecm.frontend.sa.plugins.reviewedactions;

import org.hippoecm.frontend.sa.plugin.IPluginContext;
import org.hippoecm.frontend.sa.plugin.workflow.AbstractWorkflowPlugin;
import org.hippoecm.frontend.sa.plugin.workflow.WorkflowAction;
import org.hippoecm.frontend.sa.plugin.workflow.WorkflowPlugin;
import org.hippoecm.frontend.sa.service.IFactoryService;
import org.hippoecm.frontend.sa.service.IViewService;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.reviewedactions.BasicReviewedActionsWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditingReviewedActionsWorkflowPlugin extends AbstractWorkflowPlugin {
    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(EditingReviewedActionsWorkflowPlugin.class);

    public EditingReviewedActionsWorkflowPlugin() {
        addWorkflowAction("save", "Save", new WorkflowAction() {
            private static final long serialVersionUID = 1L;

            public void execute(Workflow wf) throws Exception {
                BasicReviewedActionsWorkflow workflow = (BasicReviewedActionsWorkflow) wf;
                workflow.commitEditableInstance();
                close();
            }
        });
        addWorkflowAction("revert", "Revert", new WorkflowAction() {
            private static final long serialVersionUID = 1L;

            public void execute(Workflow wf) throws Exception {
                BasicReviewedActionsWorkflow workflow = (BasicReviewedActionsWorkflow) wf;
                workflow.disposeEditableInstance();
                close();
            }
        });
    }

    private void close() {
        IPluginContext context = getPluginContext();
        IViewService viewer = context.getService(getPluginConfig().getString(WorkflowPlugin.VIEWER_ID));
        if (viewer != null) {
            IFactoryService factory = context.getService(viewer.getServiceId());
            if (factory != null) {
                factory.delete(viewer);
            }
        } else {
            log.warn("No editor service found");
        }
    }
}
