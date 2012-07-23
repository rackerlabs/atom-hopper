package org.atomhopper.config;

import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author zinic
 */
public class ClasspathConfigurationResource implements ConfigurationResource<ClasspathConfigurationResource> {

   private final String resourceClasspath;

   public ClasspathConfigurationResource(String resourceClasspath) {
      this.resourceClasspath = resourceClasspath;
   }

   @Override
   public boolean updated() throws IOException {
      // By default we don't support updates from the classpath
      return false;
   }

   @Override
   public boolean exists() throws IOException {
      return getClass().getResource(resourceClasspath) != null;
   }

   @Override
   public String name() {
      return resourceClasspath;
   }

   @Override
   public InputStream newInputStream() throws IOException {
      final InputStream in = getClass().getResourceAsStream(resourceClasspath);

      if (in == null) {
         throw new FileNotFoundException("classpath:" + resourceClasspath);
      }

      return in;
   }
}
