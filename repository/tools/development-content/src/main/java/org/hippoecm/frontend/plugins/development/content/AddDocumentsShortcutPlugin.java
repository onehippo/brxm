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

package org.hippoecm.frontend.plugins.development.content;

import java.util.Collection;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.HeaderContributor;
import org.apache.wicket.extensions.wizard.dynamic.IDynamicWizardStep;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.frontend.dialog.AbstractDialog;
import org.hippoecm.frontend.dialog.IDialogService;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.development.content.ContentBuilder.DocumentSettings;
import org.hippoecm.frontend.plugins.development.content.wizard.DevelopmentContentWizard;
import org.hippoecm.frontend.service.render.RenderPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddDocumentsShortcutPlugin extends RenderPlugin {
    private static final long serialVersionUID = 1L;

    static final Logger log = LoggerFactory.getLogger(AddDocumentsShortcutPlugin.class);

    ContentBuilder builder;

    public AddDocumentsShortcutPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        builder = new ContentBuilder();

        add(new AjaxLink("link") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                IDialogService dialogService = getDialogService();
                dialogService.show(new AddDocumentsShortcutPlugin.Dialog());
            }

        });
    }

    public class Dialog extends AbstractDialog {
        private static final long serialVersionUID = 1L;

        public Dialog() {
            add(HeaderContributor.forCss(AddDocumentsShortcutPlugin.class, "style.css"));

            setOkLabel(new StringResourceModel("start-add-content-label", AddDocumentsShortcutPlugin.this, null));

            final DocumentSettings settings = new DocumentSettings();
            
            add(new DevelopmentContentWizard("wizard", getPluginContext(), getPluginConfig()) {
                private static final long serialVersionUID = 1L;

                @Override
                protected IDynamicWizardStep createFirstStep() {

                    return new ChooseFolderStep(null, new PropertyModel(settings, "folderUUID")) {
                        private static final long serialVersionUID = 1L;

                        public IDynamicWizardStep next() {
                            return createSecondStep(this);
                        }
                    };
                }

                private IDynamicWizardStep createSecondStep(IDynamicWizardStep previousStep) {
                    return new SelectTypesStep(previousStep, settings.nodeTypes) {
                        private static final long serialVersionUID = 1L;

                        public IDynamicWizardStep next() {
                            return createThirdStep(this);
                        }

                        @Override
                        protected Collection<String> getTypes() {
                            return builder.getDocumentTypes(settings.folderUUID);
                        }

                    };
                }

                private IDynamicWizardStep createThirdStep(IDynamicWizardStep previousStep) {
                    return new DocumentSettingsStep(previousStep, settings) {
                        private static final long serialVersionUID = 1L;

                        public IDynamicWizardStep next() {
                            return createFourthStep(this);
                        }

                    };
                }

                private IDynamicWizardStep createFourthStep(IDynamicWizardStep previousStep) {
                    return new NameSettingsStep(previousStep, settings.naming) {
                        private static final long serialVersionUID = 1L;

                        public boolean isLastStep() {
                            return true;
                        }

                        public IDynamicWizardStep next() {
                            return null;
                        }

                    };
                }

                @Override
                public void onFinish() {
                    builder.createDocuments(settings);
                    closeDialog();
                }

            });

        }

        public IModel getTitle() {
            return new StringResourceModel("add-content-label", AddDocumentsShortcutPlugin.this, null);
        }
    }

}
