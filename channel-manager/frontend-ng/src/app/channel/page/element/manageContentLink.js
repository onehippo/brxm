/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

import EmbeddedLink from './embeddedLink';

class ManageContentLink extends EmbeddedLink {
  constructor(commentElement, metaData) {
    super('manage-content-link', commentElement, metaData);
  }

  getComponentParameter() {
    return this.metaData.componentParameter;
  }

  getDefaultPath() {
    return this.metaData.defaultPath;
  }

  getRootPath() {
    return this.metaData.rootPath;
  }

  getTemplateQuery() {
    return this.metaData.templateQuery;
  }

  getComponentValue() {
    return this.metaData.componentValue;
  }

  getComponentPickerConfig() {
    if (!this.metaData.componentParameter) {
      return null;
    }
    return {
      configuration: this.metaData.componentPickerConfiguration,
      initialPath: this.metaData.componentPickerInitialPath,
      isRelativePath: this.metaData.componentParameterIsRelativePath === 'true',
      remembersLastVisited: this.metaData.componentPickerRemembersLastVisited === 'true',
      rootPath: this.metaData.componentPickerRootPath,
      selectableNodeTypes: this.metaData.componentPickerSelectableNodeTypes ?
        this.metaData.componentPickerSelectableNodeTypes.split(',') : [],
    };
  }
}

export default ManageContentLink;
