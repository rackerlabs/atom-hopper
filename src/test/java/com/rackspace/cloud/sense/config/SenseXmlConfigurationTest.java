/*
 *  Copyright 2010 Rackspace.
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
 *  under the License.
 */
package com.rackspace.cloud.sense.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author John Hopper
 */
@RunWith(Enclosed.class)
public class SenseXmlConfigurationTest {

    public static class WhenReadingInStreamConfigurations {

        private static final String CFG_RESOURCE = "/META-INF/examples/config/feed-server-config.xml";
        private SenseXmlConfiguration conf;

        @Before
        public void standUp() {
            conf = SenseXmlConfiguration.fromStream(
                    SenseXmlConfiguration.class.getResourceAsStream(CFG_RESOURCE));
        }

        @Test
        public void shouldReadConfigurations() {
            assertNotNull(conf.getRawConfig().getNamespace().getTitle());
            assertTrue(conf.getRawConfig().getNamespace().getService().size() > 0);
        }

        @Test
        public void shouldMarshalResourcesCorrectly() {
            SenseNamespaceConfiguration namespaceConfig = conf.toConfig("/base");

            assertEquals("Failed to correctly marshal URN for namespace", "/namespace", namespaceConfig.getBaseUrn());
            assertEquals("Failed to correctly marshal URN for service", "/namespace/service_a", namespaceConfig.getById("feedService1").getBaseUrn());
            assertEquals("Failed to correctly marshal URN for feed", "/namespace/service_a/feed_1", namespaceConfig.getById("feedService1").getById("feed_1").getBaseUrn());
        }
    }
}
