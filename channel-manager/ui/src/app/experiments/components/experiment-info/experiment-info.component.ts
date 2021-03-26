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

import { Component, Input, OnInit } from '@angular/core';

import { ExperimentState } from '../../models/experiment-state.enum';
import { Experiment } from '../../models/experiment.model';

@Component({
  selector: 'em-experiment-info',
  templateUrl: 'experiment-info.component.html',
  styleUrls: ['experiment-info.component.scss'],
})
export class ExperimentInfoComponent implements OnInit {
  @Input()
  experiment!: Experiment;

  @Input()
  isXPageComponent!: boolean;

  publishType!: { type: string };

  ngOnInit(): void {
    this.publishType = {
      type: this.isXPageComponent ? 'page' : 'channel',
    };
  }

  get isExperimentCreated(): boolean {
    return this.experiment.state === ExperimentState.Created;
  }

  get isExperimentCompleted(): boolean {
    return this.experiment.state === ExperimentState.Completed;
  }
}
