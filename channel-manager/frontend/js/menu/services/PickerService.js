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
    "use strict";

    angular.module('hippo.channel.menu')

        .service('hippo.channel.menu.PickerService', [
            'hippo.channel.ConfigService',
            '$http',
            function (ConfigService, $http) {
                var menuData = {
                        items: []
                    }, callObj = {
                        method: 'GET',
                        params: {
                            'FORCE_CLIENT_HOST': true,
                            'antiCache': 1424352715097
                        }
                    };
                function getDataById(id) {
                    callObj.url = '/site/_rp/' + id;
                    return $http(callObj).success(function (returnedData) {
                        addCollapsedProperties(returnedData.data, true);
                        menuData.items.push(returnedData.data);
                    });
                }

                function getData(item) {
                    callObj.url = '/site/_rp/' + item.id;
                    return $http(callObj).success(function (returnedData) {
                        addCollapsedProperties(returnedData.data, true);
                        item.items = returnedData.data.items;
                    });
                }

                function addCollapsedProperties(items, collapsed) {
                    if(Array.isArray(items)) {
                        angular.forEach(items, function (item) {
                            if(item.items && item.hasFolders) {
                                item.collapsed = collapsed;
                                addCollapsedProperties(item.items, collapsed);
                            }
                        });
                    } else {
                        items.collapsed = collapsed;
                        addCollapsedProperties(items.items, collapsed);
                    }
                }

                return {
                    getTree: function() {
                        return menuData.items;
                    },
                    getDataById: function(id) {
                        return getDataById(id);
                    },
                    getData: function(item) {
                        return getData(item);
                    }
                };
            }
        ]);
}());
