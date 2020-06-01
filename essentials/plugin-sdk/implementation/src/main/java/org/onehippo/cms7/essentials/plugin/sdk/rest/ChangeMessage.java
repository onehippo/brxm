/*
 * Copyright 2014-2018 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cms7.essentials.plugin.sdk.rest;

import org.onehippo.cms7.essentials.sdk.api.install.Instruction;

public class ChangeMessage {

    private final Instruction.Type type;
    private final String message;

    public ChangeMessage(final String message, final Instruction.Type type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public Instruction.Type getType() {
        return type;
    }
}
