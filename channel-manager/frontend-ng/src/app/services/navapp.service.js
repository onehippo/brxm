/*!
 * Copyright 2021 Bloomreach. All rights reserved. (https://www.bloomreach.com/)
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

import {
  connectToParent,
  getVersion,
} from '@bloomreach/navapp-communication';

class NavappService {
  constructor(
    $log,
  ) {
    'ngInject';

    this.$log = $log;

    this.childConfig = {
      apiVersion: getVersion(),
    };

    this._childApi = {
      getConfig: async () => this.childConfig,
      beforeNavigation: async () => this._resolveChildApiCall('beforeNavigation'),
      navigate: async (...args) => this._resolveChildApiCall('navigate', ...args),
      beforeLogout: async () => this._resolveChildApiCall('beforeLogout'),
      logout: async () => this._resolveChildApiCall('logout'),
    };

    // Map<string, Set<() => Promise>>
    this._apiSubscriptions = new Map();
  }

  subscribe(api, callback) {
    if (!(api in this._childApi)) {
      throw Error(`Attempted to subscribe to unknown navapp ChildApi api: ${api}`);
    }

    if (!this._apiSubscriptions.has(api)) {
      this._apiSubscriptions.set(api, new Set());
    }

    const subscriptions = this._apiSubscriptions.get(api);
    subscriptions.add(callback);

    return callback;
  }

  unsubscribe(api, callback) {
    if (!(api in this._childApi)) {
      throw Error(`Attempted to unsubscribe to unknown navapp ChildApi api: ${api}`);
    }

    const subscriptions = this._apiSubscriptions.get(api);
    subscriptions.delete(callback);
  }

  async connect() {
    if (this.connectionPromise) {
      await this.connectionPromise;
      return;
    }

    this.connectionPromise = this._establishConnectionToNavapp();
    this.api = await this.connectionPromise;
  }

  async getConfig() {
    await this.connect();
    return this.api.getConfig();
  }

  async getUserSettings() {
    await this.connect();
    const settings = await this.getConfig();
    return settings.userSettings;
  }

  async updateNavLocation(location) {
    await this.connect();
    return this.api.updateNavLocation(location);
  }

  async navigate(location) {
    await this.connect();
    return this.api.navigate(location);
  }

  async showMask() {
    await this.connect();
    return this.api.showMask();
  }

  async hideMask() {
    await this.connect();
    return this.api.hideMask();
  }

  async showBusyIndicator() {
    await this.connect();
    return this.api.showBusyIndicator();
  }

  async hideBusyIndicator() {
    await this.connect();
    return this.api.hideBusyIndicator();
  }

  async onUserActivity() {
    await this.connect();
    return this.api.onUserActivity();
  }

  async onSessionExpired() {
    await this.connect();
    return this.api.onSessionExpired();
  }

  async onError(error) {
    await this.connect();
    return this.api.onError(error);
  }

  async _establishConnectionToNavapp() {
    return connectToParent({
      parentOrigin: window.location.origin,
      methods: this._childApi,
    });
  }

  async _resolveChildApiCall(api, ...args) {
    if (!this._apiSubscriptions.has(api)) {
      // resolve with true in case that beforeNavigation was called
      return Promise.resolve(true);
    }

    const subscriptions = this._apiSubscriptions.get(api);
    const promises = [];
    subscriptions.forEach(cb => promises.push(cb(...args)));
    return Promise.all(promises);
  }
}

export default NavappService;
