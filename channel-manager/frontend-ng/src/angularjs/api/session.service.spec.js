/*
 * Copyright 2015-2016 Hippo B.V. (http://www.onehippo.com)
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

describe('SessionService', () => {
  'use strict';

  let $rootScope;
  let deferred;
  let sessionService;
  const channelMock = 'channelMock';

  beforeEach(() => {
    module('hippo-cm-api');

    inject(($q, _$rootScope_, SessionService, HstService) => {
      $rootScope = _$rootScope_;
      sessionService = SessionService;
      deferred = $q.defer();
      spyOn(HstService, 'initializeSession').and.returnValue(deferred.promise);
    });
  });

  it('should exist', () => {
    expect(sessionService).toBeDefined();
  });

  it('should always be readonly before initialization', () => {
    expect(sessionService.hasWriteAccess()).toEqual(false);
  });

  it('should resolve a promise with the channel argument when initialization is successful', () => {
    const promiseSpy = jasmine.createSpy('promiseSpy');
    sessionService
      .initialize(channelMock)
      .then(promiseSpy);

    deferred.resolve();
    $rootScope.$apply();
    expect(promiseSpy).toHaveBeenCalledWith(channelMock);
  });

  it('should reject a promise when initialization fails', () => {
    const catchSpy = jasmine.createSpy('catchSpy');
    sessionService
      .initialize(channelMock)
      .catch(catchSpy);

    deferred.reject();
    $rootScope.$apply();
    expect(catchSpy).toHaveBeenCalled();
  });

  it('should not allow anything before initializing', () => {
    expect(sessionService.hasWriteAccess()).toEqual(false);
    expect(sessionService.canManageChanges()).toEqual(false);
    expect(sessionService.canDeleteChannel()).toEqual(false);
  });

  it('should set privileges after initializing', () => {
    sessionService.initialize(channelMock);
    deferred.resolve({
      canWrite: true,
      canManageChanges: true,
      canDeleteChannel: true,
    });
    $rootScope.$apply();

    expect(sessionService.hasWriteAccess()).toEqual(true);
    expect(sessionService.canManageChanges()).toEqual(true);
    expect(sessionService.canDeleteChannel()).toEqual(true);
  });

  it('should not allow anything when no privileges are defined', () => {
    sessionService.initialize(channelMock);
    deferred.resolve();
    $rootScope.$apply();

    expect(sessionService.hasWriteAccess()).toEqual(false);
    expect(sessionService.canManageChanges()).toEqual(false);
    expect(sessionService.canDeleteChannel()).toEqual(false);
  });

  it('should call all registered callbacks upon successful initialization', () => {
    const cb1 = jasmine.createSpy('cb1');
    const cb2 = jasmine.createSpy('cb2');

    sessionService.registerInitCallback('cb1', cb1);
    sessionService.registerInitCallback('cb2', cb2);

    expect(cb1).not.toHaveBeenCalled();
    expect(cb2).not.toHaveBeenCalled();

    sessionService.initialize(channelMock);

    expect(cb1).not.toHaveBeenCalled();
    expect(cb2).not.toHaveBeenCalled();

    deferred.resolve(true);
    $rootScope.$digest();

    expect(cb1).toHaveBeenCalled();
    expect(cb2).toHaveBeenCalled();
  });

  it('should no longer call an unregistered callback', () => {
    const cb1 = jasmine.createSpy('cb1');
    const cb2 = jasmine.createSpy('cb2');

    sessionService.registerInitCallback('cb1', cb1);
    sessionService.registerInitCallback('cb2', cb2);

    sessionService.unregisterInitCallback('cb1');
    sessionService.unregisterInitCallback('cb3'); // should be ignored

    sessionService.initialize(channelMock);
    deferred.resolve(true);
    $rootScope.$digest();

    expect(cb1).not.toHaveBeenCalled();
    expect(cb2).toHaveBeenCalled();
  });
});
