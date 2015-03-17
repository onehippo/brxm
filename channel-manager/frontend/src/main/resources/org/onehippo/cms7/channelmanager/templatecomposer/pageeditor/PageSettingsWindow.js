/*
 *  Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
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
(function() {
    "use strict";

    Ext.namespace('Hippo.ChannelManager.TemplateComposer');

    Hippo.ChannelManager.TemplateComposer.PageSettingsWindow = Ext.extend(Hippo.ChannelManager.TemplateComposer.IFrameWindow, {

        constructor: function(config) {
            Ext.apply(config, {
                title: config.resources['page-settings-window-title'],
                width: 530,
                height: 476,
                modal: true,
                resizable: false,
                iframeUrl: './angular/page/index.html',
                iframeConfig: {
                    apiUrlPrefix: config.composerRestMountUrl,
                    cmsUser: config.cmsUser,
                    debug: config.debug,
                    locale: config.locale,
                    mountId: config.mountId,
                    sitemapId: config.sitemapId,
                    sitemapItemId: config.sitemapItemId,
                    antiCache: config.antiCache
                },
                performCloseHandshake: false
            });

            Hippo.ChannelManager.TemplateComposer.PageSettingsWindow.superclass.constructor.call(this, config);
        }

    });

    Ext.reg('Hippo.ChannelManager.TemplateComposer.PageSettingsWindow', Hippo.ChannelManager.TemplateComposer.PageSettingsWindow);

}());
