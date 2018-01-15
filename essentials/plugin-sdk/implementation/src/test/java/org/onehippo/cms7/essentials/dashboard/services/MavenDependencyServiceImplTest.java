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

package org.onehippo.cms7.essentials.dashboard.services;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.onehippo.cms7.essentials.ResourceModifyingTest;
import org.onehippo.cms7.essentials.dashboard.ctx.PluginContext;
import org.onehippo.cms7.essentials.dashboard.model.MavenDependency;
import org.onehippo.cms7.essentials.dashboard.model.TargetPom;
import org.onehippo.cms7.essentials.dashboard.service.MavenDependencyService;
import org.onehippo.testutils.log4j.Log4jInterceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MavenDependencyServiceImplTest extends ResourceModifyingTest {

    private final MavenDependencyService service = new MavenDependencyServiceImpl();

    @Test
    public void test_add_dependency() throws Exception {
        PluginContext context = getContext();
        File pomXml = createModifiableFile("/services/mavendependency/pom.xml", "cms/pom.xml");

        String before = contentOf(pomXml);
        assertFalse(before.contains("hippo-plugins-non-existing"));

        MavenDependency dependency = new MavenDependency();
        dependency.setGroupId("org.onehippo.cms7.essentials");
        dependency.setArtifactId("hippo-plugins-non-existing");
        dependency.setVersion("1.01.00-SNAPSHOT");

        assertFalse(service.hasDependency(context, TargetPom.CMS, dependency));
        assertTrue(service.addDependency(context, TargetPom.CMS, dependency));
        assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));
        assertTrue(service.addDependency(context, TargetPom.CMS, dependency));
        assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));

        String after = contentOf(pomXml);
        assertEquals(1, StringUtils.countMatches(after, "hippo-plugins-non-existing"));
    }

    @Test
    public void test_version_comparison() throws Exception {
        PluginContext context = getContext();
        createModifiableFile("/services/mavendependency/pom.xml", "cms/pom.xml");

        MavenDependency dependency = new MavenDependency();
        dependency.setGroupId("org.onehippo.cms7");

        // incoming version not specified - always true
        dependency.setArtifactId("artifact-no-version");
        assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));

        // incoming version specified, but existing dependency managed
        dependency.setVersion("1.2.3");
        assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));

        // incoming version parameterized and equal
        dependency.setArtifactId("artifact-parameterized-version");
        dependency.setVersion("${project.version}");
        assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));

        // incoming version parameterized and not equal
        dependency.setVersion("${another.version}");
        try (Log4jInterceptor interceptor = Log4jInterceptor.onWarn().trap(MavenDependencyServiceImpl.class).build()) {
            assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));
            assertTrue(interceptor.messages().anyMatch(m -> m.contains(
                    "Maven dependency 'Dependency {groupId=org.onehippo.cms7, artifactId=artifact-parameterized-version,"
                    + " version=${project.version}, type=jar}' already exists, checking for version '${another.version}', consider matching.")));
        }

        // incoming version specified and older
        dependency.setArtifactId("artifact-explicit-version");
        dependency.setVersion("1.3.4");
        assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));

        // incoming version specified and same
        dependency.setVersion("1.3.5-SNAPSHOT");
        assertTrue(service.hasDependency(context, TargetPom.CMS, dependency));

        // incoming version specified and newer
        dependency.setVersion("1.3.6");
        assertFalse(service.hasDependency(context, TargetPom.CMS, dependency));
    }
}
