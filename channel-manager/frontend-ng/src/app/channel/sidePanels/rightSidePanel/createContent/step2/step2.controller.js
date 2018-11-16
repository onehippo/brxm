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
    $element,
    $q,
    $scope,
    $translate,
    ContentEditor,
    ContentService,
    CreateContentService,
    DialogService,
    FeedbackService,
    RightSidePanelService,
    Step2Service,
    CmsService,
  ) {
    'ngInject';

    this.$element = $element;
    this.$q = $q;
    this.$scope = $scope;
    this.$translate = $translate;
    this.ContentEditor = ContentEditor;
    this.ContentService = ContentService;
    this.CreateContentService = CreateContentService;
    this.FeedbackService = FeedbackService;
    this.DialogService = DialogService;
    this.RightSidePanelService = RightSidePanelService;
    this.Step2Service = Step2Service;
    this.CmsService = CmsService;
  }

  $onInit() {
    this.documentIsSaved = false;
    this.switchingEditor = false;

    this.$scope.$watch('$ctrl.loading', (newValue, oldValue) => {
      if (newValue === oldValue) {
        return;
      }

      if (newValue) {
        this.RightSidePanelService.startLoading();
      } else {
        this.RightSidePanelService.stopLoading();
      }
    });
    // focus the form so key presses will reach Angular Material instead of the parent window
    this.$element.find('form').focus();
  }

  allMandatoryFieldsShown() {
    return this.ContentEditor.isEditing() && this.ContentEditor.getDocumentType().canCreateAllRequiredFields;
  }

  switchEditor() {
    this.switchingEditor = true;
    this.CmsService.publish('open-content', this.ContentEditor.getDocumentId(), 'edit');
    this.ContentEditor.close();
    this.close();
  }

  save() {
    return this.showLoadingIndicator(() => this.ContentEditor.save()
      .then(() => {
        this.form.$setPristine();
        this.documentIsSaved = true;
        this.FeedbackService.showNotification('NOTIFICATION_DOCUMENT_SAVED');
        this.ContentEditor.discardChanges()
          .then(() => this.Step2Service.saveComponentParameter())
          .then(() => {
            this.CreateContentService.finish(this.ContentEditor.getDocumentId());
          })
          .finally(() => {
            this.CmsService.reportUsageStatistic('CreateContent2Done');
          });
      }));
  }

  showLoadingIndicator(action) {
    this.loading = true;
    return this.$q.resolve(action())
      .finally(() => {
        this.loading = false;
      });
  }

  isEditing() {
    return this.ContentEditor.isEditing();
  }

  isSaveAllowed() {
    return this.form.$valid && this.allMandatoryFieldsShown();
  }

  close() {
    this.CreateContentService.stop();
  }

  getDocument() {
    return this.ContentEditor.getDocument();
  }

  confirmDiscardChanges(messageKey, titleKey) {
    if (this.ContentEditor.isKilled()) {
      return this.$q.resolve(); // editor was killed, don't show dialog
    }
    const translateParams = {
      documentName: this.ContentEditor.getDocumentDisplayName(),
    };

    const confirm = this.DialogService.confirm()
      .textContent(this.$translate.instant(messageKey, translateParams))
      .ok(this.$translate.instant('DISCARD'))
      .cancel(this.$translate.instant('CANCEL'));

    if (titleKey) {
      confirm.title(this.$translate.instant(titleKey, translateParams));
    }

    return this.DialogService.show(confirm);
  }

  uiCanExit() {
    if (this.documentIsSaved || this.switchingEditor) {
      return true;
    }
    return this.confirmDiscardChanges('CONFIRM_DISCARD_NEW_DOCUMENT', 'DISCARD_DOCUMENT')
      .then(() => {
        this.ContentEditor.deleteDocument();
        this.CmsService.reportUsageStatistic('CreateContent2Cancel');
      });
  }

  openEditNameUrlDialog() {
    this.Step2Service.openEditNameUrlDialog();
  }
}

export default Step2Controller;
