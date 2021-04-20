/*!
 * Copyright 2020-2021 Bloomreach. All rights reserved. (https://www.bloomreach.com/)
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

import { Inject, Injectable } from '@angular/core';
import { ReplaySubject } from 'rxjs';

import { Ng1ContentService, NG1_CONTENT_SERVICE } from '../../services/ng1/content.ng1.service';
import { Ng1WorkflowService, NG1_WORKFLOW_SERVICE } from '../../services/ng1/workflow.ng1.service';
import { PageStructureService } from '../../services/page-structure.service';
import { ProjectService } from '../../services/project.service';
import { Version, VersionUpdateBody } from '../models/version.model';
import { VersionsInfo } from '../models/versions-info.model';

@Injectable({
  providedIn: 'root',
})
export class VersionsService {
  private readonly versionsInfo = new ReplaySubject<VersionsInfo>(1);
  readonly versionsInfo$ = this.versionsInfo.asObservable();

  constructor(
    @Inject(NG1_CONTENT_SERVICE) private readonly ng1ContentService: Ng1ContentService,
    @Inject(NG1_WORKFLOW_SERVICE) private readonly workflowService: Ng1WorkflowService,
    private readonly pageStructureService: PageStructureService,
    private readonly projectService: ProjectService,
  ) { }

  async getVersionsInfo(documentId: string): Promise<void> {
    const branchId = this.projectService.getSelectedProjectId();
    const versionsInfo = await this.ng1ContentService.getDocumentVersionsInfo(documentId, branchId);
    this.versionsInfo.next(versionsInfo);
  }

  async getVersions(documentId: string): Promise<Version[]> {
    const branchId = this.projectService.getSelectedProjectId();

    const { versions } = await this.ng1ContentService.getDocumentVersionsInfo(documentId, branchId);

    return versions;
  }

  async updateVersion(documentId: string, versionUUID: string, body: VersionUpdateBody): Promise<void> {
    const branchId = this.projectService.getSelectedProjectId();
    return this.workflowService.updateWorkflowAction(documentId, body, branchId, 'versions', versionUUID);
  }

  isVersionFromPage(versionUUID: string): boolean {
    return versionUUID === this.pageStructureService.getUnpublishedVariantId();
  }
}
