/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Disable TSLint for imports that start with an uppercase letter
 * @see https://github.com/Microsoft/tslint-microsoft-contrib/issues/387
 */
// tslint:disable:import-name
import Penpal from 'penpal';  // TODO: mock the parent module instead of Penpal
import { PageProperties, UiScope } from './api';
import { Parent } from './parent'; // TODO: remove this import and move parent-related tests to a parent.spec.ts file
import UiExtension from './ui-extension';
// tslint:enable:import-name

jest.mock('penpal');

// save globals that may be replaced in tests
const connectToParent = Penpal.connectToParent;

// reset mocked globals after each test
afterEach(() => {
  Penpal.connectToParent = connectToParent;
  (Penpal.connectToParent as jest.Mock).mockClear();
});

describe('register', () => {
  it('connects to the parent API', () => {
    return UiExtension.register()
      .then(() => {
        expect(Penpal.connectToParent).toHaveBeenCalled();
      });
  });

  it('uses the parent origin provided as a URL search parameter', () => {
    window.history.pushState({}, 'Test Title', '/?br.parentOrigin=http%3A%2F%2Fcms.example.com%3A8080');

    return UiExtension.register()
      .then(() => {
        expect(Penpal.connectToParent).toHaveBeenCalledWith({
          parentOrigin: 'http://cms.example.com:8080',
          methods: expect.any(Object),
        });
      });
  });

  describe('on success', () => {
    let ui: UiScope;

    beforeEach(() => UiExtension.register().then(api => (ui = api)));

    it('initializes the UI properties', () => {
      expect(ui.baseUrl).toBe('https://cms.example.com');
      expect(ui.extension.config).toBe('testConfig');
      expect(ui.locale).toBe('en');
      expect(ui.timeZone).toBe('Europe/Amsterdam');
      expect(ui.user).toBe('admin');
      expect(ui.version).toBe('13.0.0');
    });

    describe('ui.channel.refresh()', () => {
      it('refreshes the current channel', () => {
        const connection = (Penpal.connectToParent as jest.Mock).mock.results[0].value;
        return connection.promise.then((parent: Parent) => {
          const refreshChannel = jest.spyOn(parent, 'refreshChannel');
          return ui.channel.refresh().then(() => {
            expect(refreshChannel).toHaveBeenCalled();
          });
        });
      });
    });

    describe('ui.channel.page.get()', () => {
      it('returns the current page', () => {
        return ui.channel.page.get()
          .then((page) => {
            expect(page.channel.id).toBe('testChannelId');
            expect(page.id).toBe('testPageId');
            expect(page.sitemapItem.id).toBe('testSitemapItemId');
            expect(page.url).toBe('http://www.example.com');
          });
      });
    });

    describe('ui.channel.page.refresh()', () => {
      it('refreshes the current page', () => {
        const connection = (Penpal.connectToParent as jest.Mock).mock.results[0].value;
        return connection.promise.then((parent: Parent) => {
          const refreshPage = jest.spyOn(parent, 'refreshPage');
          return ui.channel.page.refresh().then(() => {
            expect(refreshPage).toHaveBeenCalled();
          });
        });
      });
    });

    describe('ui.channel.page.on(\'navigate\', listener)', () => {
      let nextPage: PageProperties;

      beforeEach(() => {
        nextPage = {
          channel: {
            id: 'channelId',
          },
          id: 'pageId',
          sitemapItem: {
            id: 'sitemapItemId',
          },
          url: 'http://www.example.com/page',
        };
      });

      it('calls the listener whenever the parent emits a \'channel.page.navigate\' event', () => {
        const emitEvent = Penpal.connectToParent['mock'].calls[0][0].methods.emitEvent;
        const listener = jest.fn();

        ui.channel.page.on('navigate', listener);

        return emitEvent('channel.page.navigate', nextPage)
          .then(() => {
            expect(listener).toHaveBeenCalled();
            expect(listener.mock.calls[0][0]).toBe(nextPage);
          });
      });

      it('returns an unbind function', () => {
        const emitEvent = Penpal.connectToParent['mock'].calls[0][0].methods.emitEvent;
        const listener = jest.fn();

        const unbind = ui.channel.page.on('navigate', listener);
        unbind();

        return emitEvent('channel.page.navigate', nextPage)
          .then(() => {
            expect(listener).not.toHaveBeenCalled();
          });
      });
    });
  });

  describe('on failure', () => {
    it('rejects with error code "NotInIframe" when there is no parent', () => {
      Penpal.connectToParent = () => {
        // as generated by penpal
        throw Object.assign(
          new Error('connectToParent() must be called within an iframe'),
          { code: Penpal.ERR_NOT_IN_IFRAME },
        );
      };

      return UiExtension.register()
        .catch((error) => {
          expect(error).toBeInstanceOf(Error);
          expect(error.code).toBe('NotInIframe');
        });
    });

    it('rejects with error code "IncompatibleParent" when the parent does not implement getProperties()', () => {
      const incompatibleParent = {
        getPropertiesWithOtherName: () => {
        },
      };
      Penpal.connectToParent = jest.fn(() => ({ promise: Promise.resolve(incompatibleParent) }));

      return UiExtension.register()
        .catch((error) => {
          expect(error).toBeInstanceOf(Error);
          expect(error.code).toBe('IncompatibleParent');
        });
    });

    it('rejects with error code "ConnectionDestroyed" when the parent connection is destroyed', () => {
      const destroyedParent = {
        getProperties: () => {
          // as generated by penpal
          throw Object.assign(
            new Error('Unable to send getProperties() call due to destroyed connection'),
            { code: Penpal.ERR_CONNECTION_DESTROYED },
          );
        },
      };
      Penpal.connectToParent = jest.fn(() => ({ promise: Promise.resolve(destroyedParent) }));

      return UiExtension.register()
        .catch((error) => {
          expect(error).toBeInstanceOf(Error);
          expect(error.code).toBe('ConnectionDestroyed');
        });
    });

    it('rejects with error code "InternalError" when parent.getProperties rejects', () => {
      const errorParent = {
        getProperties: () => Promise.reject('eek'),
      };
      Penpal.connectToParent = jest.fn(() => ({ promise: Promise.resolve(errorParent) }));

      return UiExtension.register()
        .catch((error) => {
          expect(error).toBeInstanceOf(Error);
          expect(error.code).toBe('InternalError');
        });
    });
  });
});
