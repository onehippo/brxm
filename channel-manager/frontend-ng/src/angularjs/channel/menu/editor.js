/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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

import { menuEditorDirective } from './editor.directive';
import { MenuEditorCtrl } from './editor.controller';
import { uiTreeModule } from '../../ui/tree/tree.js';

export const channelMenuModule = angular
  .module('hippo-cm.channel.menu', [uiTreeModule.name, 'focus-if'])
  .directive('menuEditor', menuEditorDirective)
  .controller('MenuEditorCtrl', MenuEditorCtrl);
