/*
 * Copyright 2014 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.dashboard.model.hst;

import java.util.ArrayList;
import java.util.List;

import org.onehippo.cms7.essentials.dashboard.utils.annotations.PersistentCollection;
import org.onehippo.cms7.essentials.dashboard.utils.annotations.PersistentNode;

/**
 * @version "$Id$"
 */
@PersistentNode(type = "hst:templates")
public class HstTemplates extends BaseJcrModel {

    @PersistentCollection
    private List<HstTemplate> templateList = new ArrayList<>();

    public HstTemplates() {
        setName("hst:templates");
    }

    public void addTemplate(final HstTemplate template) {
        templateList.add(template);
        template.setParentPath(getParentPath() + '/' + getName());
    }

    public List<HstTemplate> getTemplateList() {
        return templateList;
    }

    public void setTemplateList(final List<HstTemplate> templateList) {
        this.templateList = templateList;
    }
}
