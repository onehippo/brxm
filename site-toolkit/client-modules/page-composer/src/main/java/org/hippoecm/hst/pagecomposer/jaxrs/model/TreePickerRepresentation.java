/*
 * Copyright 2015 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hippoecm.hst.pagecomposer.jaxrs.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.hippoecm.hst.configuration.hosting.MatchException;
import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.configuration.hosting.VirtualHost;
import org.hippoecm.hst.configuration.sitemap.HstSiteMap;
import org.hippoecm.hst.configuration.sitemap.HstSiteMapItem;
import org.hippoecm.hst.core.container.ContainerConstants;
import org.hippoecm.hst.core.linking.HstLink;
import org.hippoecm.hst.core.linking.HstLinkCreator;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.hippoecm.hst.core.request.ResolvedMount;
import org.hippoecm.hst.core.request.ResolvedSiteMapItem;
import org.hippoecm.hst.pagecomposer.jaxrs.services.PageComposerContextService;
import org.hippoecm.hst.util.HstSiteMapUtils;
import org.hippoecm.repository.HippoStdNodeType;
import org.hippoecm.repository.api.HippoNode;
import org.hippoecm.repository.util.NodeIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hippoecm.repository.HippoStdNodeType.NT_PUBLISHABLESUMMARY;
import static org.hippoecm.repository.api.HippoNodeType.NT_DOCUMENT;
import static org.hippoecm.repository.api.HippoNodeType.NT_HANDLE;

public class TreePickerRepresentation {

    private static final Logger log = LoggerFactory.getLogger(TreePickerRepresentation.class);

    private static final TreePickerRepresentationComparator comparator = new TreePickerRepresentationComparator();

    public enum PickerType {
        DOCUMENTS("documents"),
        PAGES("pages");

        private final String name;
        PickerType(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static PickerType fromName(String name) {
            if ("pages".equals(name)) {
                return PickerType.PAGES;
            } else {
                return PickerType.DOCUMENTS;
            }
        }
    }

    private PickerType pickerType = PickerType.DOCUMENTS;
    private String id;
    private String nodeName;
    private String displayName;
    private String nodePath;
    private String pathInfo;
    private boolean selectable;
    private boolean selected;
    private boolean collapsed = true;
    private boolean folder;
    private String state;
    private boolean containsFolders;
    private boolean containsDocuments;
    private List<TreePickerRepresentation> items = new ArrayList<>();

    public TreePickerRepresentation() {
    }

    private TreePickerRepresentation(final PickerType pickerType) {
        this.pickerType = pickerType;
    }

    public TreePickerRepresentation representRequestConfigNode(final PageComposerContextService pageComposerContextService)  throws RepositoryException {
        final ExpandedNodeHierarchy singleNodeHierarchy = ExpandedNodeHierarchy.createSingleNodeHierarchy(pageComposerContextService.getRequestConfigNode(NT_DOCUMENT));
        return represent(pageComposerContextService, singleNodeHierarchy, true, null);
    }

    public TreePickerRepresentation representExpandedParentTree(final PageComposerContextService pageComposerContextService,
                                                                final String siteMapPathInfo) throws RepositoryException {

        HttpSession session = pageComposerContextService.getRequestContext().getServletRequest().getSession(false);
        try {
            if (session != null) {
                String renderingHost = (String) session.getAttribute(ContainerConstants.RENDERING_HOST);
                final VirtualHost virtualHost = pageComposerContextService.getRequestContext().getResolvedMount().getMount().getVirtualHost();
                final ResolvedMount resolvedMount = virtualHost.getVirtualHosts().matchMount(renderingHost, null, siteMapPathInfo);

                final ResolvedSiteMapItem resolvedSiteMapItem = resolvedMount.matchSiteMapItem(siteMapPathInfo);

                if (resolvedSiteMapItem.getPathInfo().equals(resolvedMount.getMount().getPageNotFound())) {
                    // siteMapPathInfo item is resolved to the page not found item. this is an invalid item in the sitemap
                    // item tree
                    final String msg = String.format("For 'siteMapPathInfo %s' the resolved sitemap item '%s' is the page not " +
                                    "found sitemap item for which no tree picker representation can be created.",
                            siteMapPathInfo, resolvedSiteMapItem.getHstSiteMapItem());
                    throw new IllegalStateException(msg);
                }

                if (StringUtils.isEmpty(resolvedSiteMapItem.getRelativeContentPath())) {
                    // if explicit sitemap item, return sitemap item representation
                    // if sitemap item contains wildcards, the siteMapPathInfo is invalid as it cannot be represented in the
                    // document OR sitemap tree
                    if (resolvedSiteMapItem.getHstSiteMapItem().isExplicitElement()) {
                        return representExpandedParentTree(pageComposerContextService, resolvedSiteMapItem.getHstSiteMapItem());
                    }
                    final String msg = String.format("For 'siteMapPathInfo %s' the resolved sitemap item '%s' does not have a relative content path and " +
                            "is not an explicit sitemap item hence no tree picker representation can be created for it be " +
                            "created for it.", siteMapPathInfo, resolvedSiteMapItem.getHstSiteMapItem());
                    throw new IllegalStateException(msg);
                }

                final String contentRootPath = pageComposerContextService.getEditingMount().getContentPath();
                final Session jcrSession = pageComposerContextService.getRequestContext().getSession();
                final String selectedPath = contentRootPath + "/" + resolvedSiteMapItem.getRelativeContentPath();
                final ExpandedNodeHierarchy expandedNodeHierarchy = ExpandedNodeHierarchy.createExpandedNodeHierarchy(jcrSession,
                        contentRootPath, Collections.singletonList(selectedPath));
                return represent(pageComposerContextService, expandedNodeHierarchy, true, selectedPath);
            }
        } catch (PathNotFoundException | MatchException | IllegalStateException e) {
            if (log.isDebugEnabled()) {
                String msg = String.format("Exception trying to return document representation for siteMapPathInfo '%s'. Return root " +
                        "content folder representation instead", siteMapPathInfo);
                log.info(msg, e);
            } else {
                log.info("Exception trying to return document representation for siteMapPathInfo '{}' : {}. Return root " +
                        "content folder representation instead", siteMapPathInfo, e.toString());
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                String msg = String.format("Exception trying to return document representation for siteMapPathInfo '%s'. Return root " +
                        "content folder representation instead", siteMapPathInfo);
                log.warn(msg, e);
            } else {
                log.warn("Exception trying to return document representation for siteMapPathInfo '{}' : {}. Return root " +
                        "content folder representation instead", siteMapPathInfo, e.toString());
            }
        }
        final ExpandedNodeHierarchy singleNodeHierarchy = ExpandedNodeHierarchy.createSingleNodeHierarchy(pageComposerContextService.getRequestConfigNode(NT_DOCUMENT));
        return represent(pageComposerContextService, singleNodeHierarchy, true, null);
    }

    public TreePickerRepresentation representExpandedParentTree(final PageComposerContextService pageComposerContextService,
                                                                final HstSiteMapItem hstSiteMapItem) {
        // TODO HSTTWO-3225
        return new TreePickerRepresentation(PickerType.PAGES);
    }

    public TreePickerRepresentation represent(final PageComposerContextService pageComposerContextService,
                                              final HstSiteMap hstSiteMap) {
        // TODO HSTTWO-3225
        return new TreePickerRepresentation(PickerType.PAGES);
    }

    public TreePickerRepresentation represent(final PageComposerContextService pageComposerContextService,
                                              final HstSiteMapItem hstSiteMapItem) {
        // TODO HSTTWO-3225
        return new TreePickerRepresentation(PickerType.PAGES);
    }

    private TreePickerRepresentation represent(final PageComposerContextService pageComposerContextService,
                                               final ExpandedNodeHierarchy expandedNodeHierarchy,
                                               final boolean includeChildren, final String selectedPath) throws RepositoryException {

        final Node node = expandedNodeHierarchy.getNode();
        if (node.isNodeType(NT_DOCUMENT) && node.getParent().isNodeType(NT_HANDLE)) {
            throw new IllegalArgumentException(String.format("Node '%s' is document node. Representation only gets done until the '%s' node",
                    node.getPath(), NT_HANDLE));
        }

        id = node.getIdentifier();
        nodeName = node.getName();
        displayName = ((HippoNode) node).getLocalizedName();
        nodePath = node.getPath();

        if (nodePath.equals(selectedPath)) {
            selected = true;
        }

        if (includeChildren && !selected) {
            collapsed = false;
        }

        if (node.isNodeType(NT_HANDLE)) {
            final Node document = JcrUtils.getNodeIfExists(node, node.getName());
            if (document != null &&
                    (document.isNodeType(NT_PUBLISHABLESUMMARY) || document.isNodeType(NT_PUBLISHABLESUMMARY))) {
                state = document.getProperty(HippoStdNodeType.HIPPOSTD_STATESUMMARY).getString();
            }
        } else {
            folder = true;
        }

        final HstRequestContext requestContext = pageComposerContextService.getRequestContext();
        final HstLinkCreator linkCreator = requestContext.getHstLinkCreator();

        final Mount editingMount = pageComposerContextService.getEditingMount();
        final HstLink hstLink = linkCreator.create(node, editingMount);
        if (hstLink.isNotFound() || node.isSame(node.getSession().getNode(editingMount.getContentPath()))) {
            selectable = false;
        } else {
            selectable = true;
            pathInfo = hstLink.getPath();
            if (StringUtils.isEmpty(pathInfo)) {
                // homepage. However we need the sitemap reference path to the homepage sitemap item
                pathInfo = HstSiteMapUtils.getPath(editingMount, editingMount.getHomePage());
            }
        }

        if (!node.isNodeType(NT_HANDLE)) {
            for (Node child : new NodeIterable(node.getNodes())) {
                try {
                    ExpandedNodeHierarchy childHierarchy = expandedNodeHierarchy.getChildren().get(child.getPath());
                    if (child.isNodeType(NT_DOCUMENT)) {
                        containsFolders = true;
                    } else if (child.isNodeType(NT_HANDLE)) {
                        containsDocuments = true;
                    } else {
                        log.debug("Skipping child node '{}' that is not a folder or handle.", child.getPath());
                        continue;
                    }
                    if (selected) {
                        log.debug("Item '{}' is selected so we do not load the children.", node.getPath());
                        continue;
                    }
                    if (childHierarchy == null) {
                        if (includeChildren) {
                            ExpandedNodeHierarchy childOnly = ExpandedNodeHierarchy.createSingleNodeHierarchy(child);
                            TreePickerRepresentation childRepresentation = new TreePickerRepresentation()
                                    .represent(pageComposerContextService, childOnly, false, selectedPath);
                            items.add(childRepresentation);
                        }
                    } else {
                        boolean includeChildrenForChild = !child.isNodeType(NT_HANDLE);
                        TreePickerRepresentation childRepresentation = new TreePickerRepresentation()
                                .represent(pageComposerContextService, childHierarchy, includeChildrenForChild, selectedPath);
                        items.add(childRepresentation);
                    }

                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.warn("Exception while trying to add child '{}'.", child.getPath(), e);
                    } else {
                        log.warn("Exception while trying to add child '{}' : {}", child.getPath(), e.toString());
                    }
                }
                // else ignore
            }
        }

        final boolean jcrOrder = node.getPrimaryNodeType().hasOrderableChildNodes();
        if (!jcrOrder && isFolder()) {
            // order alphabetically, first folders then documents
            Collections.sort(items, comparator);
        }

        return this;
    }

    public String getPickerType() {
        return pickerType.getName();
    }

    public void setPickerType(final String pickerTypeName) {
        this.pickerType = PickerType.fromName(pickerTypeName);
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(final String nodePath) {
        this.nodePath = nodePath;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(final String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(final boolean selected) {
        this.selected = selected;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(final boolean collapsed) {
        this.collapsed = collapsed;
    }

    public void setSelectable(final boolean selectable) {
        this.selectable = selectable;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(final boolean folder) {
        this.folder = folder;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }


    public boolean isContainsFolders() {
        return containsFolders;
    }

    public void setContainsFolders(final boolean containsFolders) {
        this.containsFolders = containsFolders;
    }

    public boolean isContainsDocuments() {
        return containsDocuments;
    }

    public void setContainsDocuments(final boolean containsDocuments) {
        this.containsDocuments = containsDocuments;
    }

    public List<TreePickerRepresentation> getItems() {
        return items;
    }

    public void setItems(final List<TreePickerRepresentation> items) {
        this.items = items;
    }

    public static class TreePickerRepresentationComparator implements Comparator<TreePickerRepresentation> {
        @Override
        public int compare(final TreePickerRepresentation o1, final TreePickerRepresentation o2) {
            if (o1.isFolder()) {
                if (!o2.isFolder()) {
                    // folders are ordered first
                    return -1;
                }
            }
            if (o2.isFolder()) {
                if (!o1.isFolder()) {
                    // folders are ordered first
                    return 1;
                }
            }
            // both are a folder or both are a document. Return lexical sorting on displayname
            return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
        }
    }

    public static class ExpandedNodeHierarchy {

        private Node node;
        private final Map<String, ExpandedNodeHierarchy> children = new HashMap<>();

        private ExpandedNodeHierarchy() {
        }

        public static ExpandedNodeHierarchy createSingleNodeHierarchy(final Node node) {
            ExpandedNodeHierarchy single = new ExpandedNodeHierarchy();
            single.node = node;
            return single;
        }

        public static ExpandedNodeHierarchy createExpandedNodeHierarchy(final Session session,
                                                                        final String rootContentPath,
                                                                        final List<String> expandedPaths) throws RepositoryException {
            ExpandedNodeHierarchy hierarchy = new ExpandedNodeHierarchy();

            hierarchy.node = session.getNode(rootContentPath);
            for (String expandedPath : expandedPaths) {
                if (expandedPath.equals(rootContentPath)) {
                    continue;
                }
                if (!expandedPath.startsWith(rootContentPath + "/")) {
                    log.warn("Cannot expand hierarchy to path '{}' because not a descendant of '{}'", expandedPath, rootContentPath);
                    continue;
                }

                String relativePath = expandedPath.substring(rootContentPath.length() + 1);
                appendChild(relativePath, hierarchy);
            }
            return hierarchy;
        }

        public Node getNode() {
            return node;
        }

        public Map<String, ExpandedNodeHierarchy> getChildren() {
            return children;
        }

        private static void appendChild(final String relativePath, final ExpandedNodeHierarchy parent) throws RepositoryException {
            String childName = StringUtils.substringBefore(relativePath, "/");
            if (!parent.node.hasNode(childName)) {
                log.info("Cannot find childName '{}' for node '{}'.", childName, parent.node.getPath());
                return;
            }

            Node child = parent.node.getNode(childName);

            ExpandedNodeHierarchy childHierarchy = parent.children.get(child.getPath());
            if (childHierarchy == null) {
                childHierarchy = new ExpandedNodeHierarchy();
                childHierarchy.node = child;
                parent.children.put(child.getPath(), childHierarchy);
            }

            final String remaining = StringUtils.substringAfter(relativePath, "/");
            if (StringUtils.isNotEmpty(remaining)) {
                childHierarchy.appendChild(remaining, childHierarchy);
            }
        }

    }
}
