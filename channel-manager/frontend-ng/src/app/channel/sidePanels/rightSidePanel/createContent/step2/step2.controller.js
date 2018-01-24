/*
 * Copyright 2017-2018 Hippo B.V. (http://www.onehippo.com)
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

class Step2Controller {
  constructor(
    $translate,
    ContentEditor,
    ContentService,
    CreateContentService,
    FeedbackService,
    Step2Service,
  ) {
    'ngInject';

    this.$translate = $translate;
    this.ContentEditor = ContentEditor;
    this.ContentService = ContentService;
    this.CreateContentService = CreateContentService;
    this.FeedbackService = FeedbackService;
    this.Step2Service = Step2Service;
  }

  $onInit() {
    this.documentIsSaved = false;
  }

  save() {
    this.ContentEditor.save()
      .then(() => {
        this.documentIsSaved = true;
        this.CreateContentService.finish(this.ContentEditor.getDocumentId());
      });
  }

  close() {
    this.CreateContentService.stop();
  }

  getDocument() {
    return this.ContentEditor.getDocument();
  }

  uiCanExit() {
    if (this.documentIsSaved) {
      return true;
    }
    return this.ContentEditor.confirmDiscardChanges('CONFIRM_DISCARD_NEW_DOCUMENT', 'DISCARD_DOCUMENT')
      .then(async () => {
        try {
          await this.ContentService.deleteDocument(this.ContentEditor.getDocumentId());
        } catch (error) {
          const errorKey = this.$translate.instant(`ERROR_${error.data.reason}`);
          this.FeedbackService.showError(errorKey, error.data.params);
        }
        return true;
      });
  }

  openEditNameUrlDialog() {
    this.Step2Service.openEditNameUrlDialog();
  }
}

export default Step2Controller;
