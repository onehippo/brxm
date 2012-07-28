/*
 *  Copyright 2008 Hippo.
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
package org.hippoecm.hst.site.container;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.hippoecm.hst.site.addon.module.model.ModuleDefinition;
import org.hippoecm.hst.site.container.session.HttpSessionCreatedEvent;
import org.hippoecm.hst.site.container.session.HttpSessionDestroyedEvent;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpSession;

public class TestSpringComponentManagerWithAddonModules {
    
    private static final String WITH_ADDON_MODULES = "/META-INF/assembly/with-addon-modules.xml";
    
    private static Logger log = LoggerFactory.getLogger(TestSpringComponentManagerWithAddonModules.class);
    
    @Test
    public void testWithModuleDefinitions() throws Exception {
        Configuration configuration = new PropertiesConfiguration(getClass().getResource("/META-INF/assembly/with-addon-modules-config.properties"));
        SpringComponentManager componentManager = new SpringComponentManager(configuration);
        componentManager.setConfigurationResources(new String [] { WITH_ADDON_MODULES });
        
        // addon module definitions
        List<ModuleDefinition> addonModuleDefs = new ArrayList<ModuleDefinition>();
        
        // build and add analytics module definition
        ModuleDefinition def1 = new ModuleDefinition();
        def1.setName("org.example.analytics");
        def1.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics/*.xml"));
        // build and add analytics/reports module definition
        ModuleDefinition def11 = new ModuleDefinition();
        def11.setName("reports");
        def11.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics/reports/*.xml"));
        // build and add analytics/statistics module definition
        ModuleDefinition def12 = new ModuleDefinition();
        def12.setName("statistics");
        def12.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics/statistics/*.xml"));
        def1.setModuleDefinitions(Arrays.asList(def11, def12));
        addonModuleDefs.add(def1);
        
        // build and add analytics2 module definition 
        ModuleDefinition def2 = new ModuleDefinition();
        def2.setName("org.example.analytics2");
        def2.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics2/*.xml"));
        // build and add analytics2/reports module definition
        ModuleDefinition def21 = new ModuleDefinition();
        def21.setName("reports");
        def21.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics2/reports/*.xml"));
        // build and add analytics2/statistics module definition
        ModuleDefinition def22 = new ModuleDefinition();
        def22.setName("statistics");
        def22.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics2/statistics/*.xml"));
        def2.setModuleDefinitions(Arrays.asList(def21, def22));
        addonModuleDefs.add(def2);
        
        componentManager.setAddonModuleDefinitions(addonModuleDefs);
        
        componentManager.initialize();
        componentManager.start();
        
        // from the component manager spring assemblies...
        assertEquals("[ComponentManager] Hello from with-addon-modules-config.properties", 
                componentManager.getComponent("myGreeting"));
        assertEquals("Hello from container", 
                componentManager.getComponent("containerGreeting"));
        
        // from the analytics module...
        assertEquals("[Analytics] Hello from with-addon-modules-config.properties", 
                componentManager.getComponent("myGreeting", "org.example.analytics"));
        assertEquals("Hello from analytics", 
                componentManager.getComponent("analyticsGreeting", "org.example.analytics"));
        assertNull(componentManager.getComponent("analytics2Greeting", "org.example.analytics"));
        assertEquals("Hello from container", 
                componentManager.getComponent("containerGreeting", "org.example.analytics"));
        List<String> greetingList = componentManager.getComponent("greetingList", "org.example.analytics");
        assertNotNull(greetingList);
        assertEquals(2, greetingList.size());
        assertEquals("Hello from container", greetingList.get(0));
        assertEquals("Hello from analytics", greetingList.get(1));
        
        // from the analytics/reports module...
        assertEquals("[Analytics Reports] Hello from with-addon-modules-config.properties", 
                componentManager.getComponent("myGreeting", "org.example.analytics", "reports"));
        assertEquals("Hello from analytics", 
                componentManager.getComponent("analyticsGreeting", "org.example.analytics", "reports"));
        assertNull(componentManager.getComponent("analytics2Greeting", "org.example.analytics", "reports"));
        assertEquals("Hello from analytics reports", 
                componentManager.getComponent("analyticsReportsGreeting", "org.example.analytics", "reports"));
        assertNull(componentManager.getComponent("analytics2ReportsGreeting", "org.example.analytics", "reports"));
        assertEquals("Hello from container", 
                componentManager.getComponent("containerGreeting", "org.example.analytics", "reports"));
        greetingList = componentManager.getComponent("greetingList", "org.example.analytics", "reports");
        assertNotNull(greetingList);
        assertEquals(3, greetingList.size());
        assertEquals("Hello from container", greetingList.get(0));
        assertEquals("Hello from analytics", greetingList.get(1));
        assertEquals("Hello from analytics reports", greetingList.get(2));

        // from the analytics/statistics module...
        assertEquals("[Analytics Statistics] Hello from with-addon-modules-config.properties", 
                componentManager.getComponent("myGreeting", "org.example.analytics", "statistics"));
        assertEquals("Hello from analytics", 
                componentManager.getComponent("analyticsGreeting", "org.example.analytics", "statistics"));
        assertNull(componentManager.getComponent("analytics2Greeting", "org.example.analytics", "statistics"));
        assertEquals("Hello from analytics statistics", 
                componentManager.getComponent("analyticsStatisticsGreeting", "org.example.analytics", "statistics"));
        assertNull(componentManager.getComponent("analytics2StatisticsGreeting", "org.example.analytics", "statistics"));
        assertEquals("Hello from container", 
                componentManager.getComponent("containerGreeting", "org.example.analytics", "statistics"));
        greetingList = componentManager.getComponent("greetingList", "org.example.analytics", "statistics");
        assertNotNull(greetingList);
        assertEquals(3, greetingList.size());
        assertEquals("Hello from container", greetingList.get(0));
        assertEquals("Hello from analytics", greetingList.get(1));
        assertEquals("Hello from analytics statistics", greetingList.get(2));

        // from the analytics2 module...
        assertEquals("[Analytics2] Hello from with-addon-modules-config.properties", 
                componentManager.getComponent("myGreeting", "org.example.analytics2"));
        assertEquals("Hello from analytics2", 
                componentManager.getComponent("analytics2Greeting", "org.example.analytics2"));
        assertNull(componentManager.getComponent("analyticsGreeting", "org.example.analytics2"));
        assertEquals("Hello from container", 
                componentManager.getComponent("containerGreeting", "org.example.analytics2"));
        greetingList = componentManager.getComponent("greetingList", "org.example.analytics2");
        assertNotNull(greetingList);
        assertEquals(2, greetingList.size());
        assertEquals("Hello from container", greetingList.get(0));
        assertEquals("Hello from analytics2", greetingList.get(1));

        // from the analytics2/reports module...
        assertEquals("[Analytics2 Reports] Hello from with-addon-modules-config.properties", 
                componentManager.getComponent("myGreeting", "org.example.analytics2", "reports"));
        assertEquals("Hello from analytics2", 
                componentManager.getComponent("analytics2Greeting", "org.example.analytics2", "reports"));
        assertNull(componentManager.getComponent("analyticsGreeting", "org.example.analytics2", "reports"));
        assertEquals("Hello from analytics2 reports", 
                componentManager.getComponent("analytics2ReportsGreeting", "org.example.analytics2", "reports"));
        assertNull(componentManager.getComponent("analyticsReportsGreeting", "org.example.analytics2", "reports"));
        assertEquals("Hello from container", 
                componentManager.getComponent("containerGreeting", "org.example.analytics2", "reports"));
        greetingList = componentManager.getComponent("greetingList", "org.example.analytics2", "reports");
        assertNotNull(greetingList);
        assertEquals(3, greetingList.size());
        assertEquals("Hello from container", greetingList.get(0));
        assertEquals("Hello from analytics2", greetingList.get(1));
        assertEquals("Hello from analytics2 reports", greetingList.get(2));

        // from the analytics2/statistics module...
        assertEquals("[Analytics2 Statistics] Hello from with-addon-modules-config.properties", 
                componentManager.getComponent("myGreeting", "org.example.analytics2", "statistics"));
        assertEquals("Hello from analytics2", 
                componentManager.getComponent("analytics2Greeting", "org.example.analytics2", "statistics"));
        assertNull(componentManager.getComponent("analyticsGreeting", "org.example.analytics2", "statistics"));
        assertEquals("Hello from analytics2 statistics", 
                componentManager.getComponent("analytics2StatisticsGreeting", "org.example.analytics2", "statistics"));
        assertNull(componentManager.getComponent("analyticsStatisticsGreeting", "org.example.analytics2", "statistics"));
        assertEquals("Hello from container", 
                componentManager.getComponent("containerGreeting", "org.example.analytics2", "statistics"));
        greetingList = componentManager.getComponent("greetingList", "org.example.analytics2", "statistics");
        assertNotNull(greetingList);
        assertEquals(3, greetingList.size());
        assertEquals("Hello from container", greetingList.get(0));
        assertEquals("Hello from analytics2", greetingList.get(1));
        assertEquals("Hello from analytics2 statistics", greetingList.get(2));

        componentManager.stop();
        componentManager.close();
    }

    @Test
    public void testHttpSessionApplicationEventListeners() throws Exception {
        Configuration configuration = new PropertiesConfiguration();
        SpringComponentManager componentManager = new SpringComponentManager(configuration);
        componentManager.setConfigurationResources(new String [] { WITH_ADDON_MODULES });
        
        // addon module definitions
        List<ModuleDefinition> addonModuleDefs = new ArrayList<ModuleDefinition>();
        
        // build and add analytics module definition
        ModuleDefinition def1 = new ModuleDefinition();
        def1.setName("org.example.analytics");
        def1.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics/*.xml"));
        // build and add analytics/reports module definition
        ModuleDefinition def11 = new ModuleDefinition();
        def11.setName("reports");
        def11.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics/reports/*.xml"));
        // build and add analytics/statistics module definition
        ModuleDefinition def12 = new ModuleDefinition();
        def12.setName("statistics");
        def12.setConfigLocations(Arrays.asList("classpath*:META-INF/hst-assembly/addon/org/example/analytics/statistics/*.xml"));
        def1.setModuleDefinitions(Arrays.asList(def11, def12));
        addonModuleDefs.add(def1);
        
        componentManager.setAddonModuleDefinitions(addonModuleDefs);
        
        componentManager.initialize();
        componentManager.start();

        List<String> sessionIdStore = componentManager.getComponent("sessionIdStore");
        List<String> sessionIdStore1 = componentManager.getComponent("sessionIdStore", "org.example.analytics");
        List<String> sessionIdStore11 = componentManager.getComponent("sessionIdStore", "org.example.analytics", "reports");
        List<String> sessionIdStore12 = componentManager.getComponent("sessionIdStore", "org.example.analytics", "statistics");

        assertNotNull(sessionIdStore);
        assertEquals(0, sessionIdStore.size());
        assertNotNull(sessionIdStore1);
        assertEquals(0, sessionIdStore1.size());
        assertNotNull(sessionIdStore11);
        assertEquals(0, sessionIdStore11.size());
        assertNotNull(sessionIdStore12);
        assertEquals(0, sessionIdStore12.size());

        assertNotSame(sessionIdStore, sessionIdStore1);
        assertNotSame(sessionIdStore, sessionIdStore11);
        assertNotSame(sessionIdStore, sessionIdStore12);
        assertNotSame(sessionIdStore1, sessionIdStore11);
        assertNotSame(sessionIdStore1, sessionIdStore12);
        assertNotSame(sessionIdStore11, sessionIdStore12);

        List<MockHttpSession> sessionList = new ArrayList<MockHttpSession>();

        for (int i = 0; i < 10; i++) {
            sessionList.add(new MockHttpSession());
        }

        int sessionCount = 0;

        for (MockHttpSession session : sessionList) {
            componentManager.publishEvent(new HttpSessionCreatedEvent(session));
            ++sessionCount;

            assertEquals(sessionCount, sessionIdStore.size());
            assertTrue(sessionIdStore.contains(session.getId()));
        }

        log.debug("sessionIdStore: {}", sessionIdStore);
        log.debug("sessionIdStore1: {}", sessionIdStore1);
        log.debug("sessionIdStore11: {}", sessionIdStore11);
        log.debug("sessionIdStore12: {}", sessionIdStore12);

        for (MockHttpSession session : sessionList) {
            componentManager.publishEvent(new HttpSessionDestroyedEvent(session));
            --sessionCount;

            assertEquals(sessionCount, sessionIdStore.size());
            assertFalse(sessionIdStore.contains(session.getId()));
        }

        log.debug("sessionIdStore: {}", sessionIdStore);
        log.debug("sessionIdStore1: {}", sessionIdStore1);
        log.debug("sessionIdStore11: {}", sessionIdStore11);
        log.debug("sessionIdStore12: {}", sessionIdStore12);

        assertEquals(0, sessionIdStore.size());
        assertEquals(0, sessionIdStore1.size());
        assertEquals(0, sessionIdStore11.size());
        assertEquals(0, sessionIdStore12.size());

        //
        // test publishing to add-on modules from here
        //

        MockHttpSession sessionForModule1 = new MockHttpSession();
        componentManager.publishEvent(new HttpSessionCreatedEvent(sessionForModule1), "org.example.analytics");
        assertEquals(0, sessionIdStore.size());
        assertEquals(1, sessionIdStore1.size());
        assertEquals(1, sessionIdStore11.size());
        assertEquals(1, sessionIdStore12.size());
        assertTrue(sessionIdStore1.contains(sessionForModule1.getId()));
        assertTrue(sessionIdStore11.contains(sessionForModule1.getId()));
        assertTrue(sessionIdStore12.contains(sessionForModule1.getId()));

        componentManager.publishEvent(new HttpSessionDestroyedEvent(sessionForModule1), "org.example.analytics");
        assertEquals(0, sessionIdStore.size());
        assertEquals(0, sessionIdStore1.size());
        assertEquals(0, sessionIdStore11.size());
        assertEquals(0, sessionIdStore12.size());

        MockHttpSession sessionForModule11 = new MockHttpSession();
        componentManager.publishEvent(new HttpSessionCreatedEvent(sessionForModule11), "org.example.analytics", "reports");
        assertEquals(0, sessionIdStore.size());
        assertEquals(0, sessionIdStore1.size());
        assertEquals(1, sessionIdStore11.size());
        assertEquals(0, sessionIdStore12.size());
        assertTrue(sessionIdStore11.contains(sessionForModule11.getId()));

        componentManager.publishEvent(new HttpSessionDestroyedEvent(sessionForModule11), "org.example.analytics", "reports");
        assertEquals(0, sessionIdStore.size());
        assertEquals(0, sessionIdStore1.size());
        assertEquals(0, sessionIdStore11.size());
        assertEquals(0, sessionIdStore12.size());

        MockHttpSession sessionForModule12 = new MockHttpSession();
        componentManager.publishEvent(new HttpSessionCreatedEvent(sessionForModule12), "org.example.analytics", "statistics");
        assertEquals(0, sessionIdStore.size());
        assertEquals(0, sessionIdStore1.size());
        assertEquals(0, sessionIdStore11.size());
        assertEquals(1, sessionIdStore12.size());
        assertTrue(sessionIdStore12.contains(sessionForModule12.getId()));

        componentManager.publishEvent(new HttpSessionDestroyedEvent(sessionForModule12), "org.example.analytics", "statistics");
        assertEquals(0, sessionIdStore.size());
        assertEquals(0, sessionIdStore1.size());
        assertEquals(0, sessionIdStore11.size());
        assertEquals(0, sessionIdStore12.size());

        componentManager.stop();
        componentManager.close();
    }
}
