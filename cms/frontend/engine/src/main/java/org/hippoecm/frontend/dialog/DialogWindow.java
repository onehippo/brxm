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
package org.hippoecm.frontend.dialog;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.hippoecm.frontend.Home;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.EventConsumer;
import org.hippoecm.frontend.plugin.JcrEvent;
import org.hippoecm.frontend.plugin.Plugin;
import org.hippoecm.frontend.plugin.PluginManager;

public class DialogWindow extends ModalWindow implements EventConsumer {
    private static final long serialVersionUID = 1L;

    protected JcrEvent dialogResult; 
    private JcrNodeModel nodeModel;

    public DialogWindow(String id, JcrNodeModel nodeModel, final boolean resetOnClose) {
        super(id);
        setCookieName(id);
        this.nodeModel = nodeModel;

        setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            private static final long serialVersionUID = 1L;
            public void onClose(AjaxRequestTarget target) {
                if (dialogResult != null) {
                    Plugin owningPlugin = (Plugin)findParent(Plugin.class);
                    PluginManager pluginManager = owningPlugin.getPluginManager();      
                    pluginManager.update(target, dialogResult);
                }
                if (resetOnClose) {
                    Home home = (Home) getWebPage();
                    setResponsePage(home);
                    setRedirect(true);
                }
            }
        });
    }

    public AjaxLink dialogLink(String id) {
        return new AjaxLink(id) {
            private static final long serialVersionUID = 1L;
            public void onClick(AjaxRequestTarget target) {
                show(target);
            }
        };
    }

    public void setDialogResult(JcrEvent event) {
        this.dialogResult = event;
    }
    
    public void update(AjaxRequestTarget target, JcrEvent jcrEvent) {
        this.nodeModel = jcrEvent.getModel();
    }

    public JcrNodeModel getNodeModel() {
        return nodeModel;
    }

}
