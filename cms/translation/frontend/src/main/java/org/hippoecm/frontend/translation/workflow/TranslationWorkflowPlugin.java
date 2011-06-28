/*
 *  Copyright 2010 Hippo.
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
package org.hippoecm.frontend.translation.workflow;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.apache.wicket.Component;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.Session;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.hippoecm.addon.workflow.ActionDescription;
import org.hippoecm.addon.workflow.CompatibilityWorkflowPlugin;
import org.hippoecm.addon.workflow.MenuDescription;
import org.hippoecm.addon.workflow.WorkflowDescriptorModel;
import org.hippoecm.editor.type.JcrTypeStore;
import org.hippoecm.frontend.dialog.IDialogService.Dialog;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.model.ocm.StoreException;
import org.hippoecm.frontend.plugin.IPluginContext;
import org.hippoecm.frontend.plugin.config.IPluginConfig;
import org.hippoecm.frontend.plugins.richtext.IHtmlCleanerService;
import org.hippoecm.frontend.service.IBrowseService;
import org.hippoecm.frontend.service.ISettingsService;
import org.hippoecm.frontend.service.IconSize;
import org.hippoecm.frontend.session.UserSession;
import org.hippoecm.frontend.translation.ILocaleProvider;
import org.hippoecm.frontend.translation.ILocaleProvider.HippoLocale;
import org.hippoecm.frontend.translation.ILocaleProvider.LocaleState;
import org.hippoecm.frontend.translation.components.document.FolderTranslation;
import org.hippoecm.frontend.types.IFieldDescriptor;
import org.hippoecm.frontend.types.ITypeDescriptor;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.Document;
import org.hippoecm.repository.api.HippoNode;
import org.hippoecm.repository.api.HippoWorkspace;
import org.hippoecm.repository.api.StringCodec;
import org.hippoecm.repository.api.StringCodecFactory;
import org.hippoecm.repository.api.Workflow;
import org.hippoecm.repository.api.WorkflowDescriptor;
import org.hippoecm.repository.api.WorkflowException;
import org.hippoecm.repository.api.WorkflowManager;
import org.hippoecm.repository.standardworkflow.DefaultWorkflow;
import org.hippoecm.repository.translation.HippoTranslationNodeType;
import org.hippoecm.repository.translation.TranslationWorkflow;
import org.onehippo.translate.TranslateWorkflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TranslationWorkflowPlugin extends CompatibilityWorkflowPlugin {

    private final class LanguageModel extends LoadableDetachableModel<String> {
        private static final long serialVersionUID = 1L;

        @Override
        protected String load() {
            WorkflowDescriptorModel wdm = (WorkflowDescriptorModel) TranslationWorkflowPlugin.this.getDefaultModel();
            if (wdm != null) {
                WorkflowDescriptor descriptor = (WorkflowDescriptor) wdm.getObject();
                WorkflowManager manager = ((UserSession) org.apache.wicket.Session.get()).getWorkflowManager();
                try {
                    TranslationWorkflow translationWorkflow = (TranslationWorkflow) manager.getWorkflow(descriptor);
                    return (String) translationWorkflow.hints().get("locale");
                } catch (RepositoryException ex) {
                    log.error(ex.getMessage(), ex);
                } catch (RemoteException ex) {
                    log.error(ex.getMessage(), ex);
                } catch (WorkflowException ex) {
                    log.error(ex.getMessage(), ex);
                }
            }
            return "unknown";
        }
    }

    private final class AvailableLocaleProvider implements IDataProvider<HippoLocale> {
        private final ILocaleProvider localeProvider;
        private static final long serialVersionUID = 1L;
        private transient List<HippoLocale> availableLocales;

        private AvailableLocaleProvider(ILocaleProvider localeProvider) {
            this.localeProvider = localeProvider;
        }

        private void load() {
            availableLocales = new LinkedList<HippoLocale>();
            for (String language : getAvailableLanguages()) {
                availableLocales.add(localeProvider.getLocale(language));
            }
            Collections.sort(availableLocales, new Comparator<HippoLocale>() {

                @Override
                public int compare(HippoLocale o1, HippoLocale o2) {
                    return o1.getDisplayName(getLocale()).compareTo(o2.getDisplayName(getLocale()));
                }
                
            });
        }

        public Iterator<? extends HippoLocale> iterator(int first, int count) {
            if (availableLocales == null) {
                load();
            }
            return availableLocales.subList(first, first + count).iterator();
        }

        public IModel<HippoLocale> model(HippoLocale object) {
            final String id = object.getName();
            return new LoadableDetachableModel<HippoLocale>() {
                private static final long serialVersionUID = 1L;

                @Override
                protected HippoLocale load() {
                    return localeProvider.getLocale(id);
                }

            };
        }

        public int size() {
            if (availableLocales == null) {
                load();
            }
            return availableLocales.size();
        }

        public void detach() {
            availableLocales = null;
        }
    }

    private final class DisplayTranslationAction extends WorkflowAction {
        private final String language;
        private static final long serialVersionUID = 1L;

        private DisplayTranslationAction(String id, String name, ResourceReference iconModel, String language) {
            super(id, name, iconModel);
            this.language = language;
        }

        @Override
        protected String execute(Workflow wf) throws Exception {
            IBrowseService<JcrNodeModel> browser = getBrowserService();
            if (browser != null) {
                WorkflowDescriptorModel wdm = (WorkflowDescriptorModel) TranslationWorkflowPlugin.this
                        .getDefaultModel();
                if (wdm != null) {
                    Node node;
                    try {
                        node = wdm.getNode();
                        if (node != null) {
                            Node translations = node.getNode(HippoTranslationNodeType.TRANSLATIONS);
                            HippoNode translation = (HippoNode) translations.getNode(language);
                            browser.browse(new JcrNodeModel(translation.getCanonicalNode().getParent()));
                        } else {
                            log.error("No node found for document");
                        }
                    } catch (RepositoryException e) {
                        log.error("Error retrieving translation node", e);
                    }
                } else {
                    log.error("No workflow descriptor model for document");
                }
            } else {
                log.warn("Cannot navigate to translation - configured browser.id '"
                        + getPluginConfig().getString("browser.id") + "' is invalid.");
            }
            return null;
        }
    }

    private static class TranslatedFolder {
        private final Node node;

        TranslatedFolder(Node node) {
            this.node = node;
        }

        TranslatedFolder getParent() throws RepositoryException {
            Node ancestor = node;
            do {
                ancestor = ancestor.getParent();
                if ("/content/documents".equals(ancestor.getPath())) {
                    return null;
                }
            } while (!ancestor.isNodeType(HippoTranslationNodeType.NT_TRANSLATED));
            return new TranslatedFolder(ancestor);
        }

        TranslatedFolder getSibling(String locale) throws RepositoryException {
            NodeIterator siblings = node.getNode(HippoTranslationNodeType.TRANSLATIONS).getNodes();
            while (siblings.hasNext()) {
                HippoNode sibling = (HippoNode) siblings.nextNode();
                if (locale.equals(sibling.getName())) {
                    return new TranslatedFolder(sibling.getCanonicalNode());
                }
            }
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TranslatedFolder)) {
                return false;
            }
            try {
                return ((TranslatedFolder) obj).node.isSame(node);
            } catch (RepositoryException e) {
                throw new RuntimeException("could not determine whether nodes are equivalent", e);
            }
        }

        @Override
        public int hashCode() {
            try {
                return node.getPath().hashCode();
            } catch (RepositoryException e) {
                throw new RuntimeException("could not determine path of node", e);
            }
        }
    }

    private final class AddTranslationAction extends WorkflowAction {
        private final String language;
        private final IModel<String> languageModel;
        private static final long serialVersionUID = 1L;
        public String name;
        public String url;
        boolean autoTranslateContent;
        public List<FolderTranslation> folders;

        private AddTranslationAction(String id, String name, ResourceReference iconModel, String language,
                IModel<String> languageModel) {
            super(id, name, iconModel);
            this.language = language;
            this.languageModel = languageModel;
        }

        @Override
        protected Dialog createRequestDialog() {
            try {
                Node docNode = ((WorkflowDescriptorModel) TranslationWorkflowPlugin.this.getDefaultModel()).getNode();
                url = docNode.getName();
                name = url;
                if (docNode instanceof HippoNode) {
                    name = ((HippoNode) docNode).getLocalizedName();
                }
                folders = new LinkedList<FolderTranslation>();
                Node handle = docNode.getParent();
                FolderTranslation docTranslation = JcrFolderTranslationFactory.createFolderTranslation(handle, null);
                folders.add(docTranslation);

                populateFolders(handle);

                IModel<Boolean> autoTranslateModel = null;
                autoTranslateContent = false;
                WorkflowManager manager = ((HippoWorkspace)docNode.getSession().getWorkspace()).getWorkflowManager();
                WorkflowDescriptor translateWorkflow = manager.getWorkflowDescriptor("translate", docNode);
                if (translateWorkflow != null) {
                    for (Class<Workflow> workflowInterface : translateWorkflow.getInterfaces()) {
                        if (TranslateWorkflow.class.isAssignableFrom(workflowInterface)) {
                            autoTranslateModel = new PropertyModel<Boolean>(AddTranslationAction.this, "autoTranslateContent");
                            autoTranslateContent = false; // defaulkt when translation is available
                        }
                    }
                }

                return new DocumentTranslationDialog(TranslationWorkflowPlugin.this, getPluginContext().getService(
                    ISettingsService.SERVICE_ID, ISettingsService.class), this, new StringResourceModel(
                    "translate-title", TranslationWorkflowPlugin.this, null), folders, autoTranslateModel, languageModel.getObject(),
                    language, getLocaleProvider());
            } catch (Exception e) {
                log.error("Error creating document translation dialog (" + e.getMessage() + ")", e);
                error(e.getMessage());
            }
            return null;
        }

        private void populateFolders(Node handle) throws RepositoryException {
            Node sourceFolder = handle;
            try {
                while (!sourceFolder.isNodeType(HippoTranslationNodeType.NT_TRANSLATED)) {
                    sourceFolder = sourceFolder.getParent();
                }
            } catch (ItemNotFoundException infe) {
                log.warn("Parent folder of translatable document could not be found", infe);
                return;
            } catch (AccessDeniedException ade) {
                log.warn("Parent folder of translatable document is not accessible", ade);
                return;
            }

            TranslatedFolder sourceTranslatedFolder = new TranslatedFolder(sourceFolder);

            // walk up the source tree until a translated ancestor is found
            while (sourceTranslatedFolder.getSibling(language) == null) {
                FolderTranslation ft = JcrFolderTranslationFactory.createFolderTranslation(sourceTranslatedFolder.node,
                        null);
                ft.setEditable(true);
                folders.add(ft);

                sourceTranslatedFolder = sourceTranslatedFolder.getParent();
                if (sourceTranslatedFolder == null) {
                    break;
                }
            }
            if (sourceTranslatedFolder == null) {
                throw new RepositoryException("Unable to find root folder for language " + language);
            }

            TranslatedFolder targetTranslatedFolder = sourceTranslatedFolder.getSibling(language);
            assert targetTranslatedFolder != null;
            while (sourceTranslatedFolder != null) {
                {
                    FolderTranslation ft = JcrFolderTranslationFactory.createFolderTranslation(
                            sourceTranslatedFolder.node, targetTranslatedFolder.node);
                    ft.setEditable(false);
                    folders.add(ft);
                }

                // walk up the source tree until a translated ancestor is found
                sourceTranslatedFolder = sourceTranslatedFolder.getParent();
                if (sourceTranslatedFolder == null) {
                    break;
                }
                TranslatedFolder sourceSibling = sourceTranslatedFolder.getSibling(language);
                while (sourceSibling == null) {
                    FolderTranslation ft = JcrFolderTranslationFactory.createFolderTranslation(
                            sourceTranslatedFolder.node, null);
                    ft.setEditable(false);
                    folders.add(ft);

                    sourceTranslatedFolder = sourceTranslatedFolder.getParent();
                    if (sourceTranslatedFolder == null) {
                        break;
                    }
                    sourceSibling = sourceTranslatedFolder.getSibling(language);
                }
                if (sourceTranslatedFolder == null) {
                    break;
                }
                assert sourceSibling != null;

                // walk up the target tree until a translated ancestor is found
                targetTranslatedFolder = targetTranslatedFolder.getParent();
                while (targetTranslatedFolder != null) {
                    if (targetTranslatedFolder.equals(sourceSibling)) {
                        break;
                    }
                    TranslatedFolder backLink = targetTranslatedFolder.getSibling(languageModel.getObject());
                    if (backLink != null) {
                        if (!targetTranslatedFolder.equals(sourceSibling)) {
                            break;
                        }
                    }

                    FolderTranslation ft = JcrFolderTranslationFactory.createFolderTranslation(null,
                            targetTranslatedFolder.node);
                    ft.setEditable(false);
                    folders.add(ft);

                    targetTranslatedFolder = targetTranslatedFolder.getParent();
                }
                if (targetTranslatedFolder == null || !targetTranslatedFolder.equals(sourceSibling)) {
                    break;
                }
            }
            Collections.reverse(folders);
        }

        private boolean saveFolder(FolderTranslation ft, javax.jcr.Session session) {
            if (!ft.isEditable()) {
                throw new UnsupportedOperationException("Translation is immutable");
            }
            String id = ft.getId();
            try {
                Node node = session.getNodeByIdentifier(id);
                WorkflowManager manager = ((HippoWorkspace) node.getSession().getWorkspace()).getWorkflowManager();
                TranslationWorkflow tw = (TranslationWorkflow) manager.getWorkflow("translation", node);
                String namefr = ft.getNamefr();
                String urlfr = ft.getUrlfr();
                Document translationDoc = tw.addTranslation(language, urlfr);
                if (namefr != null && !urlfr.equals(namefr)) {
                    DefaultWorkflow defaultWorkflow = (DefaultWorkflow) manager.getWorkflow("core", translationDoc);
                    defaultWorkflow.localizeName(namefr);
                }
                return true;
            } catch (RepositoryException e) {
                log.error("Could not persist folder translation for " + id + " due to " + e.getMessage());
            } catch (RemoteException e) {
                log.error("Could not contact repository when storing folder translation for " + id + " due to "
                        + e.getMessage());
            } catch (WorkflowException e) {
                log.error("Workflow prevented storing translation for " + id + " due to " + e.getMessage());
            }
            return false;
        }

        @Override
        protected String execute(Workflow wf) throws Exception {
            javax.jcr.Session session = ((UserSession) Session.get()).getJcrSession();

            for (int i = 0; i < (folders.size() - 1); i++) {
                FolderTranslation folder = folders.get(i);
                if (!folder.isEditable()) {
                    continue;
                }
                if (!saveFolder(folder, session)) {
                    return COULD_NOT_CREATE_FOLDERS;
                }
            }

            FolderTranslation docTranslation = folders.get(folders.size() - 1);
            this.name = docTranslation.getNamefr();
            this.url = docTranslation.getUrlfr();

            TranslationWorkflow workflow = (TranslationWorkflow) wf;
            Document translation = workflow.addTranslation(language, url);
            try {
                WorkflowManager manager = ((HippoWorkspace) session.getWorkspace()).getWorkflowManager();
                if (autoTranslateContent) {
                    Workflow translateWorkflow = manager.getWorkflow("translate", translation);
                    if (translateWorkflow instanceof TranslateWorkflow) {
                        Set<String> plainTextFields = new TreeSet<String>();
                        Set<String> richTextFields = new TreeSet<String>();
                        Set<String> allTextFields = new TreeSet<String>();
                        collectFields(session, null, session.getNodeByIdentifier(translation.getIdentity()).getPrimaryNodeType().getName(), plainTextFields, richTextFields);
                        allTextFields.addAll(plainTextFields);
                        allTextFields.addAll(richTextFields);
                        ((TranslateWorkflow)translateWorkflow).translate(language, allTextFields);
                        try {
                            // FIXME: the validation or automatic correction of content ought to be a repository/workflow action.  But the configration
                            // needed for htmlcleaner isn't available cleanly outside of the cms.  Additionally the infrastructure for repository-side
                            // validation has been scoped out or abandoned.
                            IHtmlCleanerService cmsSpecificCleaner = getPluginContext().getService(IHtmlCleanerService.class.getName(), IHtmlCleanerService.class);
                            if (cmsSpecificCleaner != null && false == true) {
                                Node node = session.getNodeByIdentifier(translation.getIdentity());
                                node.refresh(false);
                                for (String field : richTextFields) {
                                    Property property = node.getProperty(field);
                                    property.setValue(cmsSpecificCleaner.clean(property.getString()));
                                }
                                node.save();
                            }
                        } catch (Exception ex) {
                            // we're skipping any exception here deliberately, because the htmlcleaner throws
                            // undeclared exceptions and we do not want any feedback on this.
                        }
                    }
                }
                DefaultWorkflow defaultWorkflow = (DefaultWorkflow) manager.getWorkflow("core", translation);
                if (name != null && !url.equals(name)) {
                    String localized = getLocalizeCodec().encode(name);
                    defaultWorkflow.localizeName(localized);
                }
            } finally {
                IBrowseService<JcrNodeModel> browser = getBrowserService();
                if (browser != null) {
                    browser.browse(new JcrNodeModel(session.getNodeByUUID(translation.getIdentity())));
                } else {
                    log.warn("Cannot open newly created document - configured browser.id "
                            + getPluginConfig().getString("browser.id") + " is invalid.");
                }
            }
            return null;
        }
    }

    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    final static String COULD_NOT_CREATE_FOLDERS = "could-not-create-folders";

    private static final long serialVersionUID = 1L;

    private static Logger log = LoggerFactory.getLogger(TranslationWorkflowPlugin.class);

    public boolean hasLocale(String locale) {
        WorkflowDescriptorModel wdm = (WorkflowDescriptorModel) TranslationWorkflowPlugin.this.getDefaultModel();
        if (wdm != null) {
            Node node;
            try {
                node = wdm.getNode();
                if (node != null) {
                    Node translations = node.getNode(HippoTranslationNodeType.TRANSLATIONS);
                    return translations.hasNode(locale);
                }
            } catch (RepositoryException e) {
                log.error("Failed to determine whether model is of locale " + locale);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getAvailableLanguages() {
        WorkflowDescriptorModel wdm = (WorkflowDescriptorModel) TranslationWorkflowPlugin.this.getDefaultModel();
        if (wdm != null) {
            WorkflowDescriptor descriptor = (WorkflowDescriptor) wdm.getObject();
            WorkflowManager manager = ((UserSession) org.apache.wicket.Session.get()).getWorkflowManager();
            try {
                TranslationWorkflow translationWorkflow = (TranslationWorkflow) manager.getWorkflow(descriptor);
                return (Set<String>) translationWorkflow.hints().get("available");
            } catch (RepositoryException ex) {
                log.error(ex.getMessage(), ex);
            } catch (RemoteException ex) {
                log.error(ex.getMessage(), ex);
            } catch (WorkflowException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return Collections.emptySet();
    }

    public TranslationWorkflowPlugin(IPluginContext context, IPluginConfig config) {
        super(context, config);

        final IModel<String> languageModel = new LanguageModel();
        final ILocaleProvider localeProvider = getLocaleProvider();
        add(new MenuDescription() {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getLabel() {
                Fragment fragment = new Fragment("label", "label", TranslationWorkflowPlugin.this);
                HippoLocale locale = localeProvider.getLocale(languageModel.getObject());
                if (locale != null) {
                    ResourceReference resourceRef = locale.getIcon(IconSize.TINY, LocaleState.EXISTS);
                    fragment.add(new Image("img", resourceRef));
                    fragment.add(new Label("current-language", locale.getDisplayName(getLocale())));
                } else {
                    setVisible(false);
                    fragment.add(new Image("img", new ResourceReference(getClass(), "translate-16.png")));
                    fragment.add(new Label("current-language"));
                }
                return fragment;
            }

        });

        add(new DataView<HippoLocale>("languages", new AvailableLocaleProvider(localeProvider)) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(Item<HippoLocale> item) {
                final HippoLocale locale = item.getModelObject();
                final String language = locale.getName();
                if (!hasLocale(language)) {
                    item.add(new AddTranslationAction("language", locale.getDisplayName(getLocale()), locale.getIcon(
                            IconSize.TINY, LocaleState.AVAILABLE), language, languageModel));
                } else {
                    item.add(new DisplayTranslationAction("language", locale.getDisplayName(getLocale()), locale
                            .getIcon(IconSize.TINY, LocaleState.EXISTS), language));
                }
            }

            @Override
            protected void onDetach() {
                languageModel.detach();
                super.onDetach();
            }
        });
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        WorkflowDescriptorModel wdm = (WorkflowDescriptorModel) getDefaultModel();
        if (wdm != null) {
            WorkflowDescriptor descriptor = (WorkflowDescriptor) wdm.getObject();
            if (descriptor != null) {
                try {
                    Map<String, Serializable> hints = descriptor.hints();
                    if (hints.containsKey("addTranslation") && hints.get("addTranslation").equals(Boolean.FALSE)) {
                        this.visitChildren(new IVisitor() {

                            public Object component(Component component) {
                                if (component instanceof ActionDescription) {
                                    component.setVisible(false);
                                    return IVisitor.CONTINUE_TRAVERSAL_BUT_DONT_GO_DEEPER;
                                }
                                return IVisitor.CONTINUE_TRAVERSAL;
                            }

                        });
                    }
                } catch (RepositoryException e) {
                    log.error("Failed to analyze hints for translations workflow", e);
                }
            }
        }
    }

    protected ILocaleProvider getLocaleProvider() {
        return getPluginContext().getService(
                getPluginConfig().getString(ILocaleProvider.SERVICE_ID, ILocaleProvider.class.getName()),
                ILocaleProvider.class);
    }

    protected IBrowseService getBrowserService() {
        return getPluginContext().getService(getPluginConfig().getString(IBrowseService.BROWSER_ID, "service.browse"),
                IBrowseService.class);
    }

    protected StringCodec getLocalizeCodec() {
        ISettingsService settingsService = getPluginContext().getService(ISettingsService.SERVICE_ID,
                ISettingsService.class);
        StringCodecFactory stringCodecFactory = settingsService.getStringCodecFactory();
        return stringCodecFactory.getStringCodec("encoding.display");
    }

    private static void collectFields(javax.jcr.Session session, String relPath, String nodeType, Set<String> plainTextFields, Set<String> richTextFields) throws StoreException {
        try {
            JcrTypeStore jcrTypeStore = new JcrTypeStore();
            ITypeDescriptor type = jcrTypeStore.load(nodeType);
            for (Map.Entry<String, IFieldDescriptor> field : type.getFields().entrySet()) {
                IFieldDescriptor fieldDescriptor = field.getValue();
                if ("*".equals(fieldDescriptor.getPath())) {
                    continue;
                }
                ITypeDescriptor fieldType = fieldDescriptor.getTypeDescriptor();
                if (fieldType.getType().equals(HippoStdNodeType.NT_HTML)) {
                    richTextFields.add((relPath != null ? relPath + "/" : "") + fieldDescriptor.getPath() + "/" + HippoStdNodeType.HIPPOSTD_CONTENT);
                } else if (fieldType.getName().equals("Text") || fieldType.getName().equals("Label")) {
                    plainTextFields.add((relPath != null ? relPath + "/" : "") + fieldDescriptor.getPath());
                } else if (fieldType.getName().equals("Html")) {
                    richTextFields.add((relPath != null ? relPath + "/" : "") + fieldDescriptor.getPath());
                } else if (fieldType.isNode()) {
                    collectFields(session, (relPath != null ? relPath + "/" : "") + fieldDescriptor.getPath(), fieldType.getType(), plainTextFields, richTextFields);
                }
            }
        } catch (StoreException ex) {
            // ignore nt:base
        }
    }

}
