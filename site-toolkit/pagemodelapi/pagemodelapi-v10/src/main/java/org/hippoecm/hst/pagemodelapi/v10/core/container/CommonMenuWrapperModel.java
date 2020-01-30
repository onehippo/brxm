/*
 *  Copyright 2018 Hippo B.V. (http://www.onehippo.com)
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
package org.hippoecm.hst.pagemodelapi.v10.core.container;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import org.hippoecm.hst.core.sitemenu.CommonMenu;
import org.hippoecm.hst.pagemodelapi.v10.core.model.MetadataBaseModel;

/**
 * CommonMenu Wrapper Model to include properties of the unwrapped {@code CommonMenu} as well as metadata.
 */
class CommonMenuWrapperModel extends MetadataBaseModel {

    private final CommonMenu menu;

    public CommonMenuWrapperModel(final CommonMenu menu) {
        super();
        this.menu = menu;
    }

    @JsonUnwrapped
    public CommonMenu getMenu() {
        return menu;
    }
}
