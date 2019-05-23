/*
 * (C) Copyright 2019 Bloomreach. All rights reserved. (https://www.bloomreach.com)
 */

import { Injectable } from '@angular/core';
import { ChildPromisedApi } from '@bloomreach/navapp-communication';

@Injectable({
  providedIn: 'root',
})
export class ConnectionService {
  private connections: Map<string, ChildPromisedApi> = new Map();

  addConnection(appURL: string, app: ChildPromisedApi): void {
    this.connections.set(appURL, app);
  }

  getConnection(id: string): ChildPromisedApi {
    if (!this.connections.has(id)) {
      throw new Error(`There is no connection to an ifrane with id = ${id}`);
    }

    return this.connections.get(id);
  }
}
