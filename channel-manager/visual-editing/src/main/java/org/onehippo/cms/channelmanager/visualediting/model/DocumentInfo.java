/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms.channelmanager.visualediting.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * This bean carries information of a document, stored in the CMS.
 * It is part of a document and can be serialized into JSON to expose it through a REST API.
 * Type {@code type} attribute refers to the document's {@link DocumentTypeSpec} by id.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentInfo {
    private Type type;                // enveloped reference to document type: { id: "namespace:typename" }

    private EditState editState = EditState.UNKNOWN;
    private String holder;            // CMS user-ID of editor
    private String holderDisplayName; // full, human-readable name of editor

    public Type getType() {
        return type;
    }

    public void setTypeId(final String id) {
        type = new Type(id);
    }

    public EditState getEditState() {
        return editState;
    }

    public void setEditState(final EditState editState) {
        this.editState = editState;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(final String holder) {
        this.holder = holder;
    }

    public String getHolderDisplayName() {
        return holderDisplayName;
    }

    public void setHolderDisplayName(final String holderDisplayName) {
        this.holderDisplayName = holderDisplayName;
    }

    private class Type {
        private String id;

        public Type(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    public enum EditState {
        AVAILABLE,
        ACCESS_DENIED,
        HELD_BY_OTHER_USER,
        LOCKED_BY_PUBLICATION_REQUEST,

        UNKNOWN
    }
}
