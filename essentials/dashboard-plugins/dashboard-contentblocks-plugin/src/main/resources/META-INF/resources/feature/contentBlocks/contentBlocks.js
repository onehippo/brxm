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

(function () {
    "use strict";

    angular.module('hippo.essentials')
        .controller('contentBlocksCtrl', function ($scope, $sce, $log, $rootScope, $http) {
            var restEndpoint = $rootScope.REST.dynamic + 'contentblocks/';

            $scope.deliberatelyTrustDangerousSnippet = $sce.trustAsHtml('<a target="_blank" href="http://content-blocks.forge.onehippo.org">Detailed documentation</a>');
            $scope.introMessage = "Content Blocks plugin provides the content/document editor an ability to add multiple pre-configured compound type blocks to a document. You can configure the available content blocks on per document type basis.";
            $scope.providerInput = "";
            $scope.baseCmsNamespaceUrl = "http://localhost:8080/cms?path=";
            $scope.providers = [];
            $scope.providerMap = {};
            $scope.fieldsModified = false;

            // delete a provider
            $scope.onDeleteProvider = function (provider) {
                $http.delete(restEndpoint + 'compounds/delete/' + provider.key).success(function (data) {
                    // reload providers, we deleted one:
                    loadProviders();
                });
            };

            // create a provider
            $scope.onAddProvider = function (providerName) {
                $scope.providerInput = "";
                $http.put(restEndpoint + 'compounds/create/' + providerName, providerName).success(function (data) {
                    // reload providers, we added new one:
                    loadProviders();
                });
            };

            /**
             * called on document save
             */
            $scope.saveBlocksConfiguration = function () {
                var payload = {"documentTypes": {"items": []}};
                payload.documentTypes.items = $scope.documentTypes;
                angular.forEach(payload.documentTypes.items, function (docType) {
                    // populate new providers
                    docType.providers.items = [];
                    angular.forEach(docType.providers.providerNames, function (newProvider) {
                        var provider = $scope.providerMap[newProvider.key];
                        docType.providers.items.push({
                            key: newProvider.key,
                            value: newProvider.value
                        });
                    });

                    // clean-up front-end-only attributes
                    delete docType.providers.providerNames;
                    delete docType.prefix;
                    delete docType.name;
                });

                $http.post(restEndpoint + 'compounds/contentblocks/create', payload).success(function (data) {
                    $scope.fieldsModified = true;
                    loadDocumentTypes();
                });
            };

            // helper function for deep-linking into the CMS document type editor.
            $scope.splitString = function (string, nb) {
                $scope.array = string.split(',');
                return $scope.result = $scope.array[nb];
            };
            $scope.init = function () {
                loadProviders();
                loadDocumentTypes();
            };
            $scope.init();
                
            // Helper functions
            function loadProviders() {
                $http.get(restEndpoint + 'compounds').success(function (data) {
                    $scope.providers = data.items;
                    
                    // (re-)initialize the provider map
                    $scope.providerMap = {};
                    angular.forEach($scope.providers, function (provider, key) {
                        $scope.providerMap[provider.key] = provider;
                    });
                });
            }
            function loadDocumentTypes() {
                $http.get(restEndpoint).success(function (data) {
                    $scope.documentTypes = data.items;
                    angular.forEach($scope.documentTypes, function (docType) {
                        var parts = docType.value.split(':');
                        docType.prefix = parts[0];
                        docType.name = parts[1];
                        docType.providers.providerNames = [];
                        angular.forEach(docType.providers.items, function (provider) {
                            docType.providers.providerNames.push($scope.providerMap[provider.key]);
                        });
                    });
                });
            }
        })
})();