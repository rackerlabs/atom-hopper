package org.atomhopper.config;

import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import com.rackspace.papi.commons.config.resource.impl.BufferedURLConfigurationResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class ConfigurationResolverTest {

   public static class WhenResolvingConfigurations {

      protected ConfigurationResolver resolver;

      @Before
      public void standUp() {
         resolver = new ConfigurationResolver("/etc/directory");
      }

      @Test
      public void shouldResolveFullFilePaths() throws Exception {
         final ConfigurationResource resource = resolver.resolve("file:/etc/directory/file.xml");

         assertTrue("Resolved resource must be a URL resource.", resource instanceof BufferedURLConfigurationResource);
         assertEquals("Resolved resource URL name must match expected.", "file:/etc/directory/file.xml", resource.name());
      }

      @Test
      public void shouldResolveClassPaths() throws Exception {
         final ConfigurationResource resource = resolver.resolve("classpath:/META-INF/schema/config/atom-hopper-config.xsd");

         assertTrue("Resolved resource must be a classpath resourec.", resource instanceof ClasspathConfigurationResource);
         assertEquals("Resolved resource URL name must match expected.", "/META-INF/schema/config/atom-hopper-config.xsd", resource.name());
      }

      @Test
      public void shouldResolveRelativeFilePaths() throws Exception {
         final ConfigurationResource resource = resolver.resolve("file.xml");

         assertTrue("Resolved resource must be a URL resource.", resource instanceof BufferedURLConfigurationResource);
         assertEquals("Resolved resource URL name must match expected.", "file:/etc/directory/file.xml", resource.name());
      }
   }
}
