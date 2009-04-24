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
package org.hippoecm.frontend.plugins.reviewedactions;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import org.hippoecm.addon.workflow.CompatibilityWorkflowPlugin;
import org.hippoecm.addon.workflow.StdWorkflow;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowDescriptor;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.reviewedactions.FullRequestWorkflow;

public class FullRequestWorkflowPlugin extends CompatibilityWorkflowPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private String state = "unknown";
    private Date schedule = null;

    WorkflowAction acceptAction;
    WorkflowAction rejectAction;
    
    public FullRequestWorkflowPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);
        add(new StdWorkflow("info", "info") {
            @Override
            protected IModel getTitle() {
                return new StringResourceModel("state-"+state, this, null, new Object[] { (schedule!=null ? schedule.toString() : "??") }, "unknown");
            }
            @Override
            protected void invoke() {
            }
        });

        add(acceptAction = new WorkflowAction("accept", new StringResourceModel("accept-request", this, null).getString(), null) {
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "workflow-accept-16.png");
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullRequestWorkflow workflow = (FullRequestWorkflow) wf;
                workflow.acceptRequest();
                return null;
            }
        });

        add(rejectAction = new WorkflowAction("reject", new StringResourceModel("reject-request", this, null).getString(), null) {
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "workflow-requestunpublish-16.png");
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullRequestWorkflow workflow = (FullRequestWorkflow) wf;
                workflow.rejectRequest(""); // FIXME
                return null;
            }
        });
    }

    @Override
    public void onModelChanged() {
        super.onModelChanged();
        schedule = null;
        state = "unknown";
        try {
            WorkflowManager manager = ((UserSession)org.apache.wicket.Session.get()).getWorkflowManager();
            WorkflowDescriptorModel workflowDescriptorModel = (WorkflowDescriptorModel)getModel();
            WorkflowDescriptor workflowDescriptor = (WorkflowDescriptor)getModelObject();
            if (workflowDescriptor != null) {
                Node documentNode = workflowDescriptorModel.getNode();

                Workflow workflow = manager.getWorkflow(workflowDescriptor);
                Map<String, Serializable> info = workflow.hints();
                if (info.containsKey("acceptRequest") && info.get("acceptRequest") instanceof Boolean) {
                    acceptAction.setVisible(((Boolean)info.get("acceptRequest")).booleanValue());
                }
                if (info.containsKey("rejectRequest") && info.get("rejectRequest") instanceof Boolean) {
                    acceptAction.setVisible(((Boolean)info.get("rejectRequest")).booleanValue());
                }

                if (documentNode.hasProperty("type")) {
                    state = documentNode.getProperty("type").getString();
                }
                if (documentNode.hasProperty("hipposched:triggers/default/hipposched:fireTime")) {
                    schedule = documentNode.getProperty("hipposched:triggers/default/hipposched:fireTime").getDate().getTime();
                } else if (documentNode.hasProperty("reqdate")) {
                    schedule = new Date(documentNode.getProperty("reqdate").getLong());
                }
            }
         } catch (WorkflowException ex) {
         } catch (RemoteException ex) {
         } catch (RepositoryException ex) {
            // unknown, maybe there are legit reasons for this, so don't emit a warning
        }
    }
}
