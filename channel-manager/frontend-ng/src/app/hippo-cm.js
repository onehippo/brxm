/*
 * Copyright 2015-2018 Hippo B.V. (http://www.onehippo.com)
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

import angular from 'angular';
import ngAnimate from 'angular-animate';
import ngDeviceDetector from 'ng-device-detector';
import ngLocalStorage from 'angular-local-storage';
import ngMaterial from 'angular-material';
import ngMessages from 'angular-messages';
import ngTranslate from 'angular-translate';
import 'angular-translate-loader-static-files';
import uiRouter from '@uirouter/angularjs';

// TODO: Move some of these toplevel modules into functional specific folders/modules
import BrowserService from './services/browser.service';
import CatalogService from './services/catalog.service';
import CmsService from './services/cms.service';
import ConfigService from './services/config.service';
import ContentService from './services/content.service';
import DialogService from './services/dialog.service';
import ExtensionService from './services/extension.service';
import HstComponentService from './services/hstComponent.service';
import HstService from './services/hst.service';
import SessionService from './services/session.service';
import SiteMapService from './services/siteMap.service';
import SiteMapItemService from './services/siteMapItem.service';
import SiteMenuService from './services/siteMenu.service';
import HstConstants from './constants/hst.constants';
import DomService from './services/dom.service';
import FeedbackService from './services/feedback.service';
import PathService from './services/path.service';
import ProjectService from './services/project.service';
import illegalCharactersDirective from './directives/illegalCharacters.directive';
import stopPropagationDirective from './directives/stopPropagation.directive';
import scrollToIfDirective from './directives/scrollToIf.directive';
import startWithSlashFilter from './filters/startWithSlash.filter';
import getByPropertyFilter from './filters/getByProperty.filter';
import incrementPropertyFilter from './filters/incrementProperty.filter';
import HippoGlobal from './services/hippoGlobal.service';

import channelModule from './channel/channel';
import factoriesModule from './factories/factories.module';
import config from './hippo-cm.config';
import run from './hippo-cm.run';

const hippoCmng = angular
  .module('hippo-cm', [
    ngAnimate,
    ngDeviceDetector,
    ngLocalStorage,
    ngMaterial,
    ngMessages,
    ngTranslate,
    uiRouter,
    channelModule.name,
    factoriesModule,
  ])
  .config(config)
  .run(run)
  .constant('HstConstants', HstConstants)
  .service('BrowserService', BrowserService)
  .service('CatalogService', CatalogService)
  .service('CmsService', CmsService)
  .service('ConfigService', ConfigService)
  .service('ContentService', ContentService)
  .service('DialogService', DialogService)
  .service('ExtensionService', ExtensionService)
  .service('HstComponentService', HstComponentService)
  .service('HstService', HstService)
  .service('SessionService', SessionService)
  .service('SiteMapService', SiteMapService)
  .service('SiteMapItemService', SiteMapItemService)
  .service('SiteMenuService', SiteMenuService)
  .service('DomService', DomService)
  .service('FeedbackService', FeedbackService)
  .service('PathService', PathService)
  .service('ProjectService', ProjectService)
  .service('HippoGlobal', HippoGlobal)
  .directive('illegalCharacters', illegalCharactersDirective)
  .directive('stopPropagation', stopPropagationDirective)
  .directive('scrollToIf', scrollToIfDirective)
  .filter('getByProperty', getByPropertyFilter)
  .filter('incrementProperty', incrementPropertyFilter)
  .filter('startWithSlash', startWithSlashFilter);

export default hippoCmng;
