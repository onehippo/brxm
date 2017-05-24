/*
 * Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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

package org.onehippo.cm.model.impl;

import java.util.stream.Collectors;

import org.onehippo.cm.model.ConfigurationItem;
import org.onehippo.cm.model.Definition;
import org.onehippo.cm.model.Group;
import org.onehippo.cm.model.Module;
import org.onehippo.cm.model.Project;
import org.onehippo.cm.model.Source;

public class ModelUtils {
    public static String formatModule(final Module module) {
        final Project project = module.getProject();
        final Group group = project.getGroup();

        return String.format("%s/%s/%s", group.getName(), project.getName(), module.getName());
    }

    public static String formatDefinition(final Definition definition) {
        final Source source = definition.getSource();

        return String.format("%s [%s]", formatModule(source.getModule()), source.getPath());
    }

    public static String formatDefinitions(final ConfigurationItem item) {
        return item.getDefinitions()
                .stream()
                .map(d -> ModelUtils.formatDefinition(d.getDefinition()))
                .collect(Collectors.toList())
                .toString();
    }
}
