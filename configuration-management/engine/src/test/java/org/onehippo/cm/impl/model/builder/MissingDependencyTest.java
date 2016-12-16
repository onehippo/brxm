/*
 *  Copyright 2016-2017 Hippo B.V. (http://www.onehippo.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.onehippo.cm.impl.model.builder;


import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.onehippo.cm.impl.model.builder.exceptions.MissingDependencyException;

public class MissingDependencyTest extends AbstractBaseTest {

    @Test(expected = MissingDependencyException.class)
    public void configuration_missing_dependency() {
        // config 1 depends on non existing foo
        configuration1.setAfter(ImmutableList.of("foo"));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyConfigurationDependencies(ImmutableList.of(configuration1));
    }

    @Test(expected = MissingDependencyException.class)
    public void configuration_missing_dependency_again() {
        // config 1 depends on non existing foo
        configuration1.setAfter(ImmutableList.of(configuration2.getName()));
        configuration2.setAfter(ImmutableList.of("foo"));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyConfigurationDependencies(ImmutableList.of(configuration1, configuration2));
    }

    @Test(expected = MissingDependencyException.class)
    public void project_missing_dependency() {
        // config 1 depends on non existing foo
        project1a.setAfter(ImmutableList.of("foo"));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyProjectDependencies(ImmutableList.of(project1a));
    }

    @Test(expected = MissingDependencyException.class)
    public void project_missing_dependency_again() {
        // config 1 depends on non existing foo
        project1a.setAfter(ImmutableList.of(project1b.getName()));
        project1b.setAfter(ImmutableList.of("foo"));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyProjectDependencies(ImmutableList.of(project1a, project1b));
    }

    @Test(expected = MissingDependencyException.class)
    public void module_missing_dependency() {
        // config 1 depends on non existing foo
        module1a.setAfter(ImmutableList.of("foo"));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyModuleDependencies(ImmutableList.of(module1a));
    }

    @Test(expected = MissingDependencyException.class)
    public void module_missing_dependency_again() {
        // config 1 depends on non existing foo
        module1a.setAfter(ImmutableList.of(module1b.getName()));
        module1b.setAfter(ImmutableList.of("foo"));

        ConfigurationNodeBuilder builder = new ConfigurationNodeBuilder();
        builder.verifyModuleDependencies(ImmutableList.of(module1a, module1b));
    }
}
