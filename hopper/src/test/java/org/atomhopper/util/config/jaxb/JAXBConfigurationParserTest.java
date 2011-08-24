/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.atomhopper.util.config.jaxb;

import org.junit.Ignore;
import java.net.URI;
import org.atomhopper.config.v1_0.Configuration;
import org.atomhopper.util.config.ConfigurationParserException;
import org.atomhopper.util.config.resource.ConfigurationResource;
import org.atomhopper.util.config.resource.uri.URIConfigurationResource;
import org.atomhopper.util.uri.CustomSchemeResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class JAXBConfigurationParserTest {

    public static class WhenValidatingSchemas {

        private JAXBConfigurationParser<Configuration> configurationParser;

        @Before
        public void standUp() {
            configurationParser = new JAXBConfigurationParser<Configuration>(
                    Configuration.class, org.atomhopper.config.v1_0.ObjectFactory.class);
        }

        @Test
        public void shouldLoadSchemaDefinitions() throws Exception {
            final ConfigurationResource cfgResource = new URIConfigurationResource(
                    new URI("classpath:/META-INF/schema/examples/config/feed-server-config.xml"),
                    CustomSchemeResolver.newDefaultInstance());

            configurationParser.enableValidation(getClass().getResource("/META-INF/schema/config/atom-hopper-config.xsd"));
            configurationParser.setConfigurationResource(cfgResource);

            assertNotNull("Should correctly parse a static, valid configuration", configurationParser.read());
        }

        //flashing lights are not conducive to test writing

        @Test (expected=ConfigurationParserException.class)
        public void shouldVailToParseInvalidConfigurations() throws Exception {
            final ConfigurationResource cfgResource = new URIConfigurationResource(
                    new URI("classpath:/META-INF/schema/examples/config/broken-feed-server-config.xml"),
                    CustomSchemeResolver.newDefaultInstance());

            configurationParser.enableValidation(getClass().getResource("/META-INF/schema/config/atom-hopper-config.xsd"));
            configurationParser.setConfigurationResource(cfgResource);

            configurationParser.read();
        }
    }

    @Ignore
    public static class WhenReadingConfigurations {
    }
}
