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

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.AbstractNameDialog;
import org.hippoecm.frontend.dialog.IDialogFactory;
import org.hippoecm.frontend.i18n.model.NodeTranslator;
import org.hippoecm.frontend.i18n.types.TypeTranslator;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.WorkflowsModel;
import org.hippoecm.frontend.model.nodetypes.JcrNodeTypeModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugin.workflow.AbstractWorkflowPlugin;
import org.hippoecm.frontend.plugin.workflow.WorkflowAction;
import org.hippoecm.frontend.service.IEditService;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.HippoNodeType;
import org.hippoecm.repository.api.NodeNameCodec;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.reviewedactions.FullReviewedActionsWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FullReviewedActionsWorkflowPlugin extends AbstractWorkflowPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(FullReviewedActionsWorkflowPlugin.class);

    private IModel caption = new StringResourceModel("unknown", this, null);
    private String stateSummary = "UNKNOWN";
    private boolean isLocked = false;
    private boolean pendingRequest = false;
    private Component locked;

    public FullReviewedActionsWorkflowPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        add(new Label("caption", caption));

        TypeTranslator translator = new TypeTranslator(new JcrNodeTypeModel("hippostd:publishableSummary"));
        add(new Label("status", translator.getValueName("hippostd:stateSummary", new PropertyModel(this, "stateSummary"))));

        add(locked = new org.apache.wicket.markup.html.WebMarkupContainer("locked"));

        onModelChanged();

        addWorkflowAction("edit-dialog", new StringResourceModel("edit-label", this, null), new Visibility() {
            private static final long serialVersionUID = 1L;
            public boolean isVisible() {
                return !isLocked && !pendingRequest;
            }
        }, new WorkflowAction() {
            private static final long serialVersionUID = 1L;
            // Workaround for HREPTWO-1328
            @Override
            protected void prepareSession(JcrNodeModel handleModel) throws RepositoryException {
                Node handleNode = handleModel.getNode();
                handleNode.getSession().refresh(false);
            }
            @Override
            public void execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                Document docRef = workflow.obtainEditableInstance();
		((UserSession) getSession()).getJcrSession().refresh(true);
                Node docNode = ((UserSession) getSession()).getJcrSession().getNodeByUUID(docRef.getIdentity());
                IEditService editor = getPluginContext().getService(
                        getPluginConfig().getString(IEditService.EDITOR_ID), IEditService.class);
                if (editor != null) {
                    editor.edit(new JcrNodeModel(docNode));
                } else {
                    log.warn("No editor found to edit {}", docNode.getPath());
                }
            }
        });

        addWorkflowAction("publish-dialog", new StringResourceModel("publish-label", this, null), new Visibility() {
            private static final long serialVersionUID = 1L;
            public boolean isVisible() {
                // HREPTWO-2021
                // return !(stateSummary.equals("review") || stateSummary.equals("live")) && !pendingRequest;
                return false;
            }
        }, new WorkflowAction() {
            private static final long serialVersionUID = 1L;
            // Workaround for HREPTWO-1328
            @Override
            protected void prepareSession(JcrNodeModel handleModel) throws RepositoryException {
                Node handleNode = handleModel.getNode();
                handleNode.getSession().refresh(false);
            }
            @Override
            public void execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.publish();
            }
        });

        addWorkflowAction("dePublish-dialog", new StringResourceModel("depublish-label", this, null), new Visibility() {
            private static final long serialVersionUID = 1L;

            public boolean isVisible() {
                // HREPTWO-2021
                // return !(stateSummary.equals("review") || stateSummary.equals("new")) && !pendingRequest;
                return false;
            }
        }, new WorkflowAction() {
            private static final long serialVersionUID = 1L;
            // Workaround for HREPTWO-1328
            @Override
            protected void prepareSession(JcrNodeModel handleModel) throws RepositoryException {
                Node handleNode = handleModel.getNode();
                handleNode.getSession().refresh(false);
            }
            @Override
            public void execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.depublish();
            }
        });

        IModel deleteLabel = new StringResourceModel("delete-label", this, null);
        addWorkflowDialog("delete-dialog", deleteLabel, deleteLabel, new StringResourceModel("delete-message", this,
                null, new Object[] { caption }), new WorkflowAction() {
            private static final long serialVersionUID = 1L;

            // Workaround for HREPTWO-1328
            @Override
            protected void prepareSession(JcrNodeModel handleModel) throws RepositoryException {
                Node handleNode = handleModel.getNode();
                handleNode.getSession().refresh(false);
            }

            @Override
            public void execute(Workflow wf) throws Exception {
                FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) wf;
                workflow.delete();
            }
        });

        IModel renameLabel = new StringResourceModel("rename-label", this, null);
        final StringResourceModel renameTitle = new StringResourceModel("rename-title", this, null);
        final StringResourceModel renameText = new StringResourceModel("rename-text", this, null);
        addWorkflowDialog("rename-dialog", renameLabel, new Visibility() {
            private static final long serialVersionUID = 1L;

            public boolean isVisible() {
                return true;
            }}, new IDialogFactory() {
                    private static final long serialVersionUID = 1L;

            public AbstractDialog createDialog() {

                return new AbstractNameDialog(FullReviewedActionsWorkflowPlugin.this, renameTitle, renameText, "") {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void execute() throws Exception {
                        FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) getWorkflow();
                        workflow.rename(NodeNameCodec.encode(name, true));
                    }
                };
            }
        });

        IModel schedulePublishLabel = new StringResourceModel("schedule-publish-label", this, null);
        final StringResourceModel schedulePublishTitle = new StringResourceModel("schedule-publish-title", this, null);
        final StringResourceModel schedulePublishText = new StringResourceModel("schedule-publish-text", this, null);
        addWorkflowDialog("schedule-publish-dialog", schedulePublishLabel, new Visibility() {
            private static final long serialVersionUID = 1L;

            public boolean isVisible() {
                return !(stateSummary.equals("review") || stateSummary.equals("live")) && !pendingRequest;

            }}, new IDialogFactory() {
                    private static final long serialVersionUID = 1L;

            public AbstractDialog createDialog() {

                return new AbstractDateDialog(FullReviewedActionsWorkflowPlugin.this, schedulePublishText, new Date()) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void execute() throws Exception {
                        FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) getWorkflow();
                        if (date != null) {
                            workflow.publish(date);
                        } else {
                            workflow.publish();
                        }
                    }

                    public IModel getTitle() {
                        return schedulePublishTitle;
                    }
                };
            }
        });

        IModel scheduleDePublishLabel = new StringResourceModel("schedule-depublish-label", this, null);
        final StringResourceModel scheduleDePublishTitle = new StringResourceModel("schedule-depublish-title", this, null);
        final StringResourceModel scheduleDePublishText = new StringResourceModel("schedule-depublish-text", this, null);
        addWorkflowDialog("schedule-depublish-dialog", scheduleDePublishLabel, new Visibility() {
            private static final long serialVersionUID = 1L;

            public boolean isVisible() {
                return !(stateSummary.equals("review") || stateSummary.equals("new")) && !pendingRequest;

            }}, new IDialogFactory() {
                    private static final long serialVersionUID = 1L;

            public AbstractDialog createDialog() {

                return new AbstractDateDialog(FullReviewedActionsWorkflowPlugin.this, scheduleDePublishText, new Date()) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    protected void execute() throws Exception {
                        FullReviewedActionsWorkflow workflow = (FullReviewedActionsWorkflow) getWorkflow();
                        if (date != null) {
                            workflow.depublish(date);
                        } else {
                            workflow.depublish();
                        }
                    }

                    public IModel getTitle() {
                        return scheduleDePublishTitle;
                    }
                };
            }
        });
    }

    // FIXME: duplicate implementation in BasicviewedActionsWorkflowPlugin
    @Override
    public void onModelChanged() {
        super.onModelChanged();

        WorkflowsModel model = (WorkflowsModel) getModel();
        try {
            JcrNodeModel nodeModel = model.getNodeModel();
            caption = new NodeTranslator(nodeModel).getNodeName();

            Node node = nodeModel.getNode();
            Node child = null;
            if (node.isNodeType(HippoNodeType.NT_HANDLE)) {
                pendingRequest = false;
                for (NodeIterator iter = node.getNodes("hippo:request"); iter.hasNext(); ) {
                    Node request = iter.nextNode();
                        if(request.isNodeType(HippoNodeType.NT_REQUEST)) {
                            if(!request.hasProperty("type") || !request.getProperty("type").getString().equals("rejected")) {
                                pendingRequest = true;
                            }
                        }
                }
                for (NodeIterator iter = node.getNodes(node.getName()); iter.hasNext(); ) {
                    child = iter.nextNode();
                    if (child.isNodeType(HippoNodeType.NT_DOCUMENT)) {
                        node = child;
                        if (child.hasProperty("hippostd:state") && child.getProperty("hippostd:state").getString().equals("draft")) {
                            break;
                        }
                    } else {
                        child = null;
                    }
                }
            }
            if (child != null && child.hasProperty("hippostd:stateSummary")) {
                stateSummary = node.getProperty("hippostd:stateSummary").getString();
            }
            isLocked = false;
            locked.setVisible(isLocked);
            if (child != null && child.hasProperty("hippostd:state") &&
                child.getProperty("hippostd:state").getString().equals("draft") && child.hasProperty("hippostd:holder") &&
                !child.getProperty("hippostd:holder").getString().equals(child.getSession().getUserID())) {
                isLocked = true;
                locked.setVisible(isLocked);
            }
        } catch (RepositoryException ex) {
            // status unknown, maybe there are legit reasons for this, so don't emit a warning
            log.info(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }
}
