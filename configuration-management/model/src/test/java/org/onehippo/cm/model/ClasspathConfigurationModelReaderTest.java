/*
 *  Copyright 2017 Hippo B.V. (http://www.onehippo.com)
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
package org.onehippo.cm.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.onehippo.cm.model.impl.ConfigurationModelImpl;
import org.onehippo.cm.model.impl.ModelTestUtils;
import org.onehippo.cm.model.impl.ModuleImpl;
import org.onehippo.cm.model.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.onehippo.cm.model.util.FilePathUtils.nativePath;

public class ClasspathConfigurationModelReaderTest extends AbstractBaseTest {

    private static final Logger log = LoggerFactory.getLogger(ClasspathConfigurationModelReaderTest.class);

    // Disable tests for now -- needs to be refactored now that ConfigurationServiceImpl loads FS modules
//    @Test
    public void load_modules_from_classpath() throws IOException, ParserException, URISyntaxException {

        Set<ModuleImpl> classpathModules = loadModules();

        assertEquals("Classpath should contain correct number of modules", 1,
                classpathModules.size());

        Set<ModuleImpl> expectedModules = new HashSet<>();
        final ModuleImpl testModule = ModelTestUtils.makeModule("test-module");
        expectedModules.add(testModule);

        assertTrue("Classpath should contain test-module", classpathModules.containsAll(expectedModules));

        ModuleImpl loadedTestModule = classpathModules.stream().filter(module -> module.equals(testModule)).findFirst().get();

        // the test-module loaded from classpath should have a single source with a single definition,
        // since that's what is contained in the files at the root of test-classes
        assertSource(loadedTestModule, "test.yaml", 1);
    }

//    @Test
    public void load_modules_from_classpath_and_filesystem() throws IOException, ParserException, URISyntaxException {
        try {
            // set system properties to trigger developer mode that loads config from source files
            System.setProperty("project.basedir", calculateBaseDir() + nativePath("/src/test/resources"));

            Set<ModuleImpl> classpathModules = loadModules();

            assertEquals("Classpath should contain correct number of modules", 1,
                    classpathModules.size());

            Set<ModuleImpl> expectedModules = new HashSet<>();
            final ModuleImpl testModule = ModelTestUtils.makeModule("test-module");
            expectedModules.add(testModule);

            assertTrue("Classpath should contain test-module", classpathModules.containsAll(expectedModules));

            ModuleImpl loadedTestModule = classpathModules.stream().filter(module -> module.equals(testModule)).findFirst().get();

            // the test-module loaded from source files should have a single source with two definitions, not one,
            // since that's what is contained in the files under TestModuleFileSource
            assertSource(loadedTestModule, "test.yaml", 2);
        }
        finally {
            // set system properties back to empty strings to make sure developer mode is off again
            System.setProperty("project.basedir", "");
        }
    }

    /**
     * Utility method to calculate correct path in case when run under Intellij IDEA (Working directory should be set to
     * module's root, e.g. ../master/engine)
     * @return base directory
     */
    private String calculateBaseDir() {
        String basedir = System.getProperty("basedir");
        basedir = basedir != null ? basedir: System.getProperty("user.dir");
        return basedir;
    }

    /**
     * Helper to load modules using a ClasspathConfigurationModelReader.
     * @return the Set of Modules loaded by the ClasspathConfigurationModelReader with current system properties
     */
    protected Set<ModuleImpl> loadModules() throws IOException, ParserException, URISyntaxException {
        ClasspathConfigurationModelReader classpathReader = new ClasspathConfigurationModelReader();
        ConfigurationModelImpl model = classpathReader.read(getClass().getClassLoader(), true);
        return model.getModulesStream().collect(Collectors.toSet());
    }
}
