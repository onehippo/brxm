/*
 *  Copyright 2009 Hippo.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.wicket.ResourceReference;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import org.hippoecm.addon.workflow.CompatibilityWorkflowPlugin;
import org.hippoecm.addon.workflow.StdWorkflow;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.frontend.dialog.IDialogService.Dialog;
import org.hippoecm.frontend.i18n.types.TypeTranslator;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.nodetypes.JcrNodeTypeModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.service.IEditorManager;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowDescriptor;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.reviewedactions.FullReviewedActionsWorkflow;

public class FullReviewedActionsWorkflowPlugin extends CompatibilityWorkflowPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(FullReviewedActionsWorkflowPlugin.class);

    public String stateSummary = "UNKNOWN";

    WorkflowAction editAction;
    WorkflowAction publishAction;
    WorkflowAction depublishAction;
    WorkflowAction deleteAction;
    WorkflowAction schedulePublishAction;
    WorkflowAction scheduleDepublishAction;

    public FullReviewedActionsWorkflowPlugin(final IPluginContext context, IPluginConfig config) {
        super(context, config);

        final TypeTranslator translator = new TypeTranslator(new JcrNodeTypeModel("hippostd:publishableSummary"));
        add(new StdWorkflow("info", "info") {
            @Override
            protected IModel getTitle() {
                return translator.getValueName("hippostd:stateSummary", new PropertyModel(FullReviewedActionsWorkflowPlugin.this, "stateSummary"));
            }
            @Override
            protected void invoke() {
            }
        });

        add(editAction = new WorkflowAction("edit", new StringResourceModel("edit-label", this, null).getString(), null) {
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "edit-16.png");
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                Document docRef = workflow.obtainEditableInstance();
                ((UserSession) getSession()).getJcrSession().refresh(true);
                Node docNode = ((UserSession) getSession()).getJcrSession().getNodeByUUID(docRef.getIdentity());
                IEditorManager editorMgr = getPluginContext().getService(getPluginConfig().getString(IEditorManager.EDITOR_ID), IEditorManager.class);
                if (editorMgr != null) {
                    editorMgr.openEditor(new JcrNodeModel(docNode));
                } else {
                    log.warn("No editor found to edit {}", docNode.getPath());
                }
                return null;
            }
        });

        add(publishAction = new WorkflowAction("publish", new StringResourceModel("publish-label", this, null).getString(), null) {
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "publish-16.png");
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.publish();
                return null;
            }
        });

        add(depublishAction = new WorkflowAction("depublish", new StringResourceModel("depublish-label", this, null).getString(), null) {
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "unplublish-16.png");
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.depublish();
                return null;
            }
        });

        add(deleteAction = new WorkflowAction("delete", new StringResourceModel("delete-label", this, null).getString(), null) {
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "delete-16.png");
            }
            @Override
            protected Dialog createRequestDialog() {
                return new WorkflowAction.WorkflowDialog(new StringResourceModel("delete-message", FullReviewedActionsWorkflowPlugin.this, null)) {
                    @Override
                    public IModel getTitle() {
                        return new StringResourceModel("delete-label", FullReviewedActionsWorkflowPlugin.this, null);
                    }};
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.delete();
                return null;
            }
        });

        add(schedulePublishAction = new WorkflowAction("schedulePublish", new StringResourceModel("schedule-publish-label", this, null).getString(), null) {
            public Date date = new Date();
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "publish-schedule-16.png");
            }
            @Override
            protected Dialog createRequestDialog() {
                return new WorkflowAction.DateDialog(new StringResourceModel("schedule-publish-text", FullReviewedActionsWorkflowPlugin.this, null)) {
                    @Override
                    public IModel getTitle() {
                        return new StringResourceModel("schedule-publish-title", FullReviewedActionsWorkflowPlugin.this, null);
                    }};
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow)wf;
                if (date != null) {
                    workflow.requestPublication(date);
                } else {
                    workflow.requestPublication();
                }
                return null;
            }
        });

        add(scheduleDepublishAction = new WorkflowAction("scheduleDepublish", new StringResourceModel("schedule-depublish-label", this, null).getString(), null) {
            public Date date = new Date();
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "unpublish-scheduled-16.png");
            }
            @Override
            protected Dialog createRequestDialog() {
                return new WorkflowAction.DateDialog(new StringResourceModel("schedule-depublish-text", FullReviewedActionsWorkflowPlugin.this, null)) {
                    @Override
                    public IModel getTitle() {
                        return new StringResourceModel("schedule-depublish-title", FullReviewedActionsWorkflowPlugin.this, null);
                    }};
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow)wf;
                if (date != null) {
                    workflow.requestDepublication(date);
                } else {
                    workflow.requestDepublication();
                }
                return null;
            }
        });

        add(new WorkflowAction("move", new StringResourceModel("move-label", this, null)) {
            public String name;
            public JcrNodeModel destination;
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "move-16.png");
            }
            @Override
            protected Dialog createRequestDialog() {
                name = "";
                return new WorkflowAction.DestinationDialog(new StringResourceModel("move-title", FullReviewedActionsWorkflowPlugin.this, null), new StringResourceModel("move-text", FullReviewedActionsWorkflowPlugin.this, null), new PropertyModel(this, "name"), destination);
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.move(new Document(destination.getNode().getUUID()), NodeNameCodec.encode(name, true));
                return null;
            }
        });

        add(new WorkflowAction("rename", new StringResourceModel("rename-label", this, null)) {
            public String name;
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "rename-16.png");
            }
            @Override
            protected Dialog createRequestDialog() {
                name = "";
                return new WorkflowAction.NameDialog(new StringResourceModel("rename-title", FullReviewedActionsWorkflowPlugin.this, null), new StringResourceModel("rename-text", FullReviewedActionsWorkflowPlugin.this, null), new PropertyModel(this, "name"));
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.rename(NodeNameCodec.encode(name, true));
                return null;
            }
        });

        add(new WorkflowAction("copy", new StringResourceModel("copy-label", this, null)) {
            public String name;
            public JcrNodeModel destination;
            @Override
            protected ResourceReference getIcon() {
                return new ResourceReference(getClass(), "copy-16.png");
            }
            @Override
            protected Dialog createRequestDialog() {
                name = "";
                return new WorkflowAction.DestinationDialog(new StringResourceModel("copy-title", FullReviewedActionsWorkflowPlugin.this, null), new StringResourceModel("copy-text", FullReviewedActionsWorkflowPlugin.this, null), new PropertyModel(this, "name"), destination);
            }
            @Override
            protected String execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.copy(new Document(destination.getNode().getUUID()), NodeNameCodec.encode(name, true));
                return null;
            }
        });

        onModelChanged();
    }

    @Override
    protected void onModelChanged() {
        super.onModelChanged();
        try {
            WorkflowManager manager = ((UserSession)org.apache.wicket.Session.get()).getWorkflowManager();
            WorkflowDescriptorModel workflowDescriptorModel = (WorkflowDescriptorModel)getModel();
            WorkflowDescriptor workflowDescriptor = (WorkflowDescriptor)getModelObject();
            if(workflowDescriptor != null) {
                Node documentNode = workflowDescriptorModel.getNode();
                if(documentNode != null && documentNode.hasProperty("hippostd:stateSummary")) {
                    stateSummary = documentNode.getProperty("hippostd:stateSummary").getString();
                }
                Workflow workflow = manager.getWorkflow(workflowDescriptor);
                Map<String, Serializable> info = workflow.hints();
                if (info.containsKey("obtainEditableInstanceobtainEditableInstance") && info.get("obtainEditableInstanceobtainEditableInstance") instanceof Boolean && !((Boolean)info.get("obtainEditableInstanceobtainEditableInstance")).booleanValue()) {
                     editAction.setVisible(false);
                }
                if (info.containsKey("publish") && info.get("publish") instanceof Boolean && !((Boolean)info.get("publish")).booleanValue()) {
                   publishAction.setVisible(false);
                    schedulePublishAction.setVisible(false);
                }
                if (info.containsKey("depublish") && info.get("depublish") instanceof Boolean && !((Boolean)info.get("depublish")).booleanValue()) {
                    depublishAction.setVisible(false);
                    scheduleDepublishAction.setVisible(false);
                }
                if (info.containsKey("delete") && info.get("delete") instanceof Boolean && !((Boolean)info.get("delete")).booleanValue()) {
                    deleteAction.setVisible(false);
                }
            }
        } catch (RepositoryException ex) {
            log.error(ex.getMessage(), ex);
        } catch (WorkflowException ex) {
            log.error(ex.getMessage(), ex);
        } catch (RemoteException ex) {
            log.error(ex.getMessage(), ex);
        }
    }
}
