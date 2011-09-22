package org.atomhopper.util.config.resource.uri;

import org.atomhopper.util.uri.CustomSchemeResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class URIConfigurationResourceTest {

    private static final String VALID_CLASSPATH_URI = "classpath:/META-INF/schema/config/bindings.xjb";

    public static class WhenGettingInputStreamsFromURILocations {

        private URIConfigurationResource configurationResource;

        @Before
        public void standUp() throws Exception {
            configurationResource = new URIConfigurationResource(
                    new URI(VALID_CLASSPATH_URI),
                    CustomSchemeResolver.newDefaultInstance());
        }

        @Test
        public void shouldGenerateInputStreamWithValidLocatableURIs() throws Exception {
            final InputStream result = configurationResource.getInputStream();

            assertNotNull("InputStream returned by the static classpath URI, \""
                    + VALID_CLASSPATH_URI
                    + "\" must not be null", result);

            assertTrue("InputStream should have at least one byte readable", result.read() > 0);
            
            result.close();
        }
    }
}
