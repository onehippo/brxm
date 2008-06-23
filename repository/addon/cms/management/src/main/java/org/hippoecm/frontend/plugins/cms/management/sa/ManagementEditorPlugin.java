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
package org.hippoecm.frontend.plugins.cms.management.sa;

import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.sa.template.editor.EditorForm;
import org.hippoecm.frontend.sa.template.editor.EditorPlugin;

public class ManagementEditorPlugin extends EditorPlugin {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private FeedbackPanel feedback;

    public ManagementEditorPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        add(feedback = new FeedbackPanel("feedback"));
        feedback.setOutputMarkupId(true);
    }

    @Override
    protected EditorForm newForm() {
        JcrNodeModel jcrModel = (JcrNodeModel) getModel();
        EditorForm form = new EditorForm("form", jcrModel, this, getPluginContext(), getPluginConfig());
        return form;
    }

    @Override
    public void onModelChanged() {
        super.onModelChanged();

        JcrNodeModel model = (JcrNodeModel) getModel();
        System.out.println("");
    }

//    @Override
//    public void handle(Request request) {
//        if (request.getOperation().equals("feedback")) {
//            request.getContext().addRefresh(feedback);
//        } else {
//            super.handle(request);
//        }
//    }
}
