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
package org.hippoecm.frontend.plugins.cms.browse.list;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.hippoecm.frontend.model.JcrNodeModel;
import org.hippoecm.frontend.plugins.standards.list.NodeCell;

public class DocumentListingNodeCell extends NodeCell {
    @SuppressWarnings("unused")
    private final static String SVN_ID = "$Id$";

    private static final long serialVersionUID = 1L;

    private DocumentListingPlugin plugin;

    public DocumentListingNodeCell(String id, JcrNodeModel model, String nodePropertyName, DocumentListingPlugin plugin) {
        super(id, model, nodePropertyName);
        this.plugin = plugin;
    }

    @Override
    protected void onSelect(JcrNodeModel model, AjaxRequestTarget target) {
        plugin.onSelect(model, target);
    }
}
