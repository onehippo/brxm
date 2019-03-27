/*
 * Copyright 2018-2019 Hippo B.V. (http://www.onehippo.com)
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
 * Defines all public API of the ui-extension library.
 * @module api
 */

/**
 * Callback function for events generated by the CMS.
 */
export type EventHandler<Events> = (eventData: Events[keyof Events]) => any;

/**
 * Function to unsubscribe an [[EventHandler]] with.
 */
type UnsubscribeFn = () => void;

/**
 * Identifies which error occurred while using the ui-extension library.
 */
export enum UiExtensionErrorCode {
  /**
   * The UI extension is not running in an iframe.
   */
  'NotInIframe' = 'NotInIframe',

  /**
   * The version of the CMS in which the UI extension is loaded is not compatible with the version of the
   * ui-extension library used by the UI extension.
   */
  'IncompatibleParent' = 'IncompatibleParent',

  /**
   * The connection with the CMS has been destroyed.
   */
  'ConnectionDestroyed' = 'ConnectionDestroyed',

  /**
   * An internal error occurred.
   */
  'InternalError' = 'InternalError',
}

/**
 * Error returned by the ui-extension library via a rejected Promise.
 */
export interface UiExtensionError {
  /**
   * Identifies the error to applications.
   */
  code: UiExtensionErrorCode;

  /**
   * Explains the error to humans.
   */
  message: string;
}

/**
 * Properties of the CMS that loads the UI extension.
 */
export interface UiProperties {
  /**
   * The base URL of the CMS, without any query parameters.
   */
  baseUrl: string;

  /**
   * Properties of this UI extension.
   */
  extension: {
    /**
     * The configuration of this UI extension. How to interpret the string
     * (e.g. parse as JSON) is up to the implementation of the UI extension.
     */
    config: string,
  };

  /**
   * The locale of the CMS user as selected in the login page. For example: "en".
   */
  locale: string;

  /**
   * The time zone of the CMS user as selected on the login page. For example: "Europe/Amsterdam".
   */
  timeZone: string;

  /**
   * Properties of the CMS user.
   */
  user: {
    /**
     * The username of the CMS user. For example: "admin".
     */
    id: string,

    /**
     * The first name of the CMS user. For example: "Suzanna".
     */
    firstName: string,

    /**
     * The last name of the CMS user. For example: "Doe".
     */
    lastName: string,

    /**
     * Concatenation of the first and last name of the CMS user, or the username if both are blank.
     * For example: "Suzanna Doe" or "admin".
     */
    displayName: string,
  };

  /**
   * The version of the CMS. For example: "13.0.0".
   */
  version: string;
}

/**
 * API to access information about and communicate with the CMS that loads the UI extension.
 */
export interface UiScope extends UiProperties {
  /**
   * API for the current channel.
   */
  channel: ChannelScope;

  /**
   * API for the current document.
   */
  document: DocumentScope;
}

/**
 * API to access information about and communicate with the current channel shown in the Channel Manager.
 */
export interface ChannelScope extends Emitter<ChannelScopeEvents> {
  /**
   * API for the current page in the current channel.
   */
  page: PageScope;

  /**
   * Refreshes the metadata of the currently shown channel (e.g. whether it has changes, the sitemap, etc.).
   * The Channel Manager UI will be updated to reflect the channel’s refreshed metadata.
   */
  refresh: () => Promise<void>;
}

export interface ChannelScopeEvents {
  /**
   * Triggered when a user publishes channel changes.
   * @since 13.1
   * @event
   */
  'changes.publish': void;

  /**
   * Triggered when a user discards channel changes
   * @since 13.1
   * @event
   */
  'changes.discard': void;
}

/**
 * API to access information about and communicate with the current page
 * in the current channel shown in the Channel Manager.
 */
export interface PageScope extends Emitter<PageScopeEvents> {
  /**
   * @returns a Promise that resolves with [[PageProperties]] of the current page.
   */
  get: () => Promise<PageProperties>;

  /**
   * Refreshes the page currently shown in the Channel Manager.
   */
  refresh: () => Promise<void>;
}

/**
 * An emitter of events.
 */
export interface Emitter<Events> {
  /**
   * Subscribes a handler for events emitted by the CMS. The type of the
   * emitted value depends on the emitted event.
   *
   * @param eventName the name of the emitted event.
   * @param handler the function to call with the emitted value.
   *
   * @returns a function to unsubscribe the handler again.
   */
  on: (eventName: keyof Events, handler: EventHandler<Events>) => UnsubscribeFn;
}

/**
 * A map of all events related to a page in a channel and the type of value they emit.
 */
export interface PageScopeEvents {
  /**
   * Triggered when a user navigates to another page in the Channel Manager.
   * Emits the properties of the new page.
   * @event
   */
  navigate: PageProperties;
}

/**
 * Properties of a page in a channel.
 */
export interface PageProperties {
  /**
   * Properties of the channel the page is part of.
   */
  channel: {
    /**
     * The identifier of the channel. For example: "example-preview".
     */
    id: string;
  };

  /**
   * The UUID of the `hst:component` root node of the page hierarchy.
   */
  id: string;

  /**
   * Properties of the matched sitemap item.
   */
  sitemapItem: {
    /**
     * The UUID of the sitemap item.
     */
    id: string;
  };

  /**
   * The public URL of the page.
   */
  url: string;
}

/**
 * API to access information about and communicate with the current document.
 */
export interface DocumentScope {
  /**
   * @returns a Promise that resolves with [[DocumentProperties]] of the current document.
   */
  get(): Promise<DocumentProperties>;

  /**
   * API for the current field of the current document.
   */
  field: FieldScope;
}

/**
 * Defines the different possible modes of a document editor.
 */
export enum DocumentEditorMode {
  View = 'view',
  Compare = 'compare',
  Edit = 'edit',
}

/**
 * Properties of a document.
 */
export interface DocumentProperties {
  /**
   * The UUID of the handle node.
   */
  id: string;

  /**
   * Display name of the document.
   */
  displayName: string;

  /**
   * Locale of the document, e.g. "sv". Is undefined when the document does not have a locale.
   */
  locale: string;

  /**
   * The mode of the document editor.
   */
  mode: DocumentEditorMode;

  /**
   * The URL name of the document.
   */
  urlName: string;

  /**
   * UUID of the currently shown variant, typically 'draft' or 'preview'.
   */
  variant: {
    id: string;
  };
}

/**
 * API to access information about and communicate with the current document field.
 */
export interface FieldScope {
  /**
   * Gathers current field value.
   */
  getValue(): Promise<string>;

  /**
   * Updates current field value.
   * @param value the new field value
   */
  setValue(value: string): Promise<void>;

  /**
   * Set the height of the surrounding iframe.
   * @param pixels the number of pixels
   */
  setHeight(pixels: number): Promise<void>;
}
