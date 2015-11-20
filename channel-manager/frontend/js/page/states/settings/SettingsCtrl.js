/*
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
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
(function () {
  'use strict';

  angular.module('hippo.channel.page')

    .controller('hippo.channel.page.SettingsCtrl', [
      '$scope',
      '$filter',
      'hippo.channel.FeedbackService',
      'hippo.channel.PageService',
      'hippo.channel.PrototypeService',
      'hippo.channel.ChannelService',
      'hippo.channel.ConfigService',
      'hippo.channel.Container',
      function ($scope, $filter, FeedbackService, PageService, PrototypeService, ChannelService, ConfigService, ContainerService) {
        var translate = $filter('translate');

        $scope.page = {
          id: null,
          title: '',
          primaryDocument: '',
          availableDocumentRepresentations: [],
          lastPathInfoElement: '',
          prototype: {
            id: null
          }
        };

        $scope.copy = {
          mountId : ConfigService.mountId,
          target: '',
          lastPathInfoElement: ''
        };

        $scope.state = {
          isEditable: false,
          isLocked: false
        };

        $scope.lock = {
          owner: null,
          timestamp: null
        };

        $scope.template = {
          isVisible: false
        };

        $scope.validation = {
          illegalCharacters: '/ :'
        };

        $scope.locations = [];

        $scope.channels = [];

        $scope.tooltips = {
          lastPathInfoElement: function () {
            if ($scope.form.$dirty) {
              if ($scope.form.lastPathInfoElement.$error.required) {
                return translate('URL_REQUIRED');
              } else if ($scope.form.lastPathInfoElement.$error.illegalCharacters) {
                return translate('URL_ILLEGAL_CHARACTERS', $scope.validation);
              }
            }
            return '';
          },
          copyLastPathInfoElement: function () {
            if ($scope.copyForm.$dirty) {
              if ($scope.copyForm.lastPathInfoElement.$error.required) {
                return translate('URL_REQUIRED');
              } else if ($scope.copyForm.lastPathInfoElement.$error.illegalCharacters) {
                return translate('URL_ILLEGAL_CHARACTERS', $scope.validation);
              }
            }
            return '';
          },
          deleteButton: function () {
            if ($scope.page.isHomePage) {
              return translate('TOOLTIP_IS_HOMEPAGE');
            } else if (!$scope.state.isEditable) {
              return translate('TOOLTIP_NOT_EDITABLE');
            }
            return '';
          },
          copyButton: function () {
            if (!$scope.state.isEditable) {
              return translate('TOOLTIP_NOT_COPYABLE');
            }
            return '';
          }
        };

        $scope.host = '';
        $scope.mountPath = '';
        $scope.isConfirmationVisible = false;
        $scope.isHomePage = false;

        // error feedback
        function setErrorFeedback (errorResponse) {
          $scope.errorFeedback = FeedbackService.getFeedback(errorResponse);
        }

        // fetch data
        loadHost()
          .then(loadChannels)
          .then(loadPrototypes)
          .then(loadPage);
          // TODO .then(loadLocations) and do not load the locations in loadPrototypes as it now done.
          // TODO namely, we need to be able to load locations from a different channel than the current one as well

        $scope.showAssignNewTemplate = function () {
          $scope.template.isVisible = true;
        };

        $scope.submit = function () {
          var pageModel = {
            id: $scope.page.id,
            pageTitle: $scope.page.title,
            name: $scope.page.lastPathInfoElement,
            componentConfigurationId: $scope.page.prototype.id,
            primaryDocumentRepresentation: $scope.page.primaryDocumentRepresentation
          };

          PageService.updatePage(pageModel).then(function (data) {
            ContainerService.showPage(data.renderPathInfo);
          }, setErrorFeedback);
        };

        $scope.submitCopyPage = function () {

          var copyModel = {
            mountId: $scope.copy.mountId,
            siteMapItemUUId: $scope.page.id,
            targetName: $scope.copy.lastPathInfoElement,
            targetSiteMapItemUUID: $scope.copy.target.id
          };

          PageService.copyPage(copyModel).then(function (data) {
            ContainerService.showPage(data.renderPathInfo);
          }, function (errorResponse) {
            $scope.errorFeedback = FeedbackService.getFeedback(errorResponse);
          });
        };

        $scope.copy.reloadTargets = function () {
          loadPageLocations($scope.copy.mountId);
        };

        $scope.closeContainer = function () {
          ContainerService.performClose();
        };

        $scope.deletePage = function () {
          $scope.isConfirmationVisible = true;
        };

        $scope.confirmDelete = function () {
          $scope.isConfirmationVisible = false;

          PageService.deletePage($scope.page.id).then(function () {
            ContainerService.showPage($scope.mountPath);
          }, setErrorFeedback);
        };

        $scope.cancelDelete = function () {
          $scope.isConfirmationVisible = false;
        };

        $scope.removePrimaryDocument = function () {
          $scope.isConfirmationRemovePrimaryDocumentVisible = true;
        };

        $scope.confirmRemovePrimaryDocument = function () {
          $scope.isConfirmationRemovePrimaryDocumentVisible = false;
          var pageModel = {
            id: $scope.page.id,
            pageTitle: $scope.page.title,
            name: $scope.page.lastPathInfoElement,
            componentConfigurationId: $scope.page.prototype.id,
            primaryDocumentRepresentation: {
              path: '',
              displayName: '',
              exists: false,
              document: false
            }
          };
          PageService.updatePage(pageModel).then(function (data) {
            ContainerService.showPage(data.renderPathInfo);
          }, setErrorFeedback);
        };

        $scope.cancelRemovePrimaryDocument = function () {
          $scope.isConfirmationRemovePrimaryDocumentVisible = false;
        };

        function loadHost () {
          return PageService.getMountInfo()
            .then(function (mountInfo) {
              $scope.host = mountInfo.hostName + mountInfo.mountPath;
              $scope.mountPath = mountInfo.mountPath;
              return mountInfo;
            }, setErrorFeedback);
        }

        function loadChannels () {
          return ChannelService.getChannels()
            .then(function (data) {
              $scope.channels = data;
            }, setErrorFeedback);
        }

        function loadPageLocations (mountId) {
          return ChannelService.getPageLocations(mountId)
            .then(function (data) {
              $scope.locations = data || [];
            }, setErrorFeedback);
        }

        function loadPrototypes () {
          return PrototypeService.getPrototypes()
            .then(function (data) {
              $scope.prototypes = data.prototypes;
              $scope.locations = data.locations || [];
              return data.prototypes;
            }, setErrorFeedback);
        }

        function loadPage () {
          function arrayObjectIndexOf (arr, obj) {
            for (var i = 0; i < arr.length; i++) {
              if (angular.equals(arr[i], obj)) {
                return i;
              }
            }
            return -1;
          }

          return PageService.getCurrentPage()
            .then(function (currentPage) {
              var defaultRepresentation = [
                {
                  displayName: $filter('translate')('NONE'),
                  path: ''

                }
              ];
              $scope.page.id = currentPage.id;
              $scope.page.title = currentPage.pageTitle;
              $scope.page.parentPathInfo = currentPage.parentPathInfo;
              $scope.page.availableDocumentRepresentations = defaultRepresentation.concat(currentPage.availableDocumentRepresentations);

              if (currentPage.primaryDocumentRepresentation) {
                var indexOfPrimaryDoc = arrayObjectIndexOf($scope.page.availableDocumentRepresentations, currentPage.primaryDocumentRepresentation);
                $scope.page.primaryDocumentRepresentation = $scope.page.availableDocumentRepresentations[indexOfPrimaryDoc];
              } else {
                $scope.page.primaryDocumentRepresentation = $scope.page.availableDocumentRepresentations[0];
              }

              $scope.page.lastPathInfoElement = currentPage.name;
              $scope.copy.lastPathInfoElement = currentPage.name;

              for(var i = 0; i < $scope.locations.length; i++) {
                if (currentPage.parentLocation && $scope.locations[i].id === currentPage.parentLocation.id) {
                  $scope.copy.target = $scope.locations[i];
                }
              }

              $scope.page.hasContainerItem = currentPage.hasContainerItemInPageDefinition;
              $scope.page.isHomePage = currentPage.isHomePage;

              // pages are only editable when the sitemap item is:
              // 1. located in the HST workspace
              // 2. the page is not the homepage
              // 3. the page is not locked by someone else
              $scope.state.isLocked = angular.isString(currentPage.lockedBy) && currentPage.lockedBy !== ConfigService.cmsUser;
              $scope.state.isEditable = !$scope.page.isHomePage && !$scope.state.isLocked && currentPage.workspaceConfiguration;

              // lock information
              $scope.lock.owner = currentPage.lockedBy;
              $scope.lock.timestamp = currentPage.lockedOn;

              return currentPage;
            }, setErrorFeedback);
        }
      }
    ]);
}());
