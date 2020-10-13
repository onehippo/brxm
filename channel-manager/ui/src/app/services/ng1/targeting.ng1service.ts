/*!
 * Copyright 2020 Bloomreach. All rights reserved. (https://www.bloomreach.com/)
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

import { InjectionToken } from '@angular/core';

import { ExperimentGoal } from '../../experiments/models/experiment-goal.model';
import { ExperimentStatus } from '../../experiments/models/experiment-status.model';
import { Experiment } from '../../experiments/models/experiment.model';
import { TargetingApiResponse } from '../../models/targeting-api-response.model';
import { Characteristic } from '../../variants/models/characteristic.model';
import { Persona } from '../../variants/models/persona.model';
import { Variant, VariantCharacteristicData } from '../../variants/models/variant.model';

export interface Ng1TargetingService {
  getCharacteristics(): Promise<TargetingApiResponse<void>>;
  getPersonas(): Promise<TargetingApiResponse<{ items: Persona[] }>>;
  getVariants(containerItemId: string): Promise<TargetingApiResponse<Variant[]>>;
  deleteVariant(componentId: string, variantId: string): Promise<TargetingApiResponse<any>>;
  addVariant(
    componentId: string,
    formData: any,
    personaId?: string,
    characteristics?: VariantCharacteristicData[],
  ): Promise<TargetingApiResponse<any>>;
  getGoals(): Promise<TargetingApiResponse<ExperimentGoal[]>>;
  getExperiment(componentId: string): Promise<TargetingApiResponse<Experiment>>;
  getExperimentStatus(experimentId: string): Promise<TargetingApiResponse<ExperimentStatus>>;
  saveExperiment(componentId: string, goalId: string, variantId: string): Promise<TargetingApiResponse<string>>;
  completeExperiment(componentId: string, keepOnlyVariantId: string): Promise<TargetingApiResponse<void>>;
  getCharacteristics(): Promise<TargetingApiResponse<Characteristic[]>>;
  getCharacteristicConfig(id: string): any | null;
}

export const NG1_TARGETING_SERVICE = new InjectionToken<Ng1TargetingService>('NG1_TARGETING_SERVICE');
