/*
 * Copyright 2008 Hippo
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
package org.hippoecm.frontend.plugins.standardworkflow.dialogs;

import javax.jcr.RepositoryException;

import org.apache.wicket.model.PropertyModel;
import org.hippoecm.frontend.legacy.dialog.AbstractWorkflowDialog;
import org.hippoecm.frontend.legacy.dialog.DialogWindow;
import org.hippoecm.frontend.legacy.plugin.channel.Channel;
import org.hippoecm.frontend.legacy.plugin.channel.Request;
import org.hippoecm.frontend.model.JcrItemModel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.widgets.TextFieldWidget;
import org.hippoecm.repository.standardworkflow.EditmodelWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyModelDialog extends AbstractWorkflowDialog {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(CopyModelDialog.class);

    private String name;

    public CopyModelDialog(DialogWindow dialogWindow) {
        super(dialogWindow, "Copy model");
        if (dialogWindow.getNodeModel().getNode() == null) {
            ok.setEnabled(false);
        }

        try {
            name = dialogWindow.getNodeModel().getNode().getName();
        } catch (RepositoryException ex) {
            log.error(ex.getMessage());
        }

        add(new TextFieldWidget("name", new PropertyModel(this, "name")));
    }

    @Override
    protected void execute() throws Exception {
        EditmodelWorkflow workflow = (EditmodelWorkflow) getWorkflow();
        if (workflow != null) {
            String path = workflow.copy(name);
            JcrNodeModel nodeModel = new JcrNodeModel(new JcrItemModel(path));
            if (path != null) {
                Channel channel = getChannel();
                if (channel != null) {
                    Request request = channel.createRequest("flush", nodeModel.getParentModel());
                    channel.send(request);
                    // HREPTWO-749:
                    // request = channel.createRequest("edit", nodeModel);
                    // channel.send(request);
                } else {
                    log.error("could not send edit message");
                }
            } else {
                log.error("no model found to edit");
            }
        } else {
            log.error("no workflow defined on model for selected node");
        }
    }

    @Override
    public void cancel() {
    }

}
