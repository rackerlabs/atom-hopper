package org.atomhopper.config;

import com.rackspace.papi.commons.config.resource.ConfigurationResource;
import com.rackspace.papi.commons.config.resource.ConfigurationResourceResolver;
import com.rackspace.papi.commons.config.resource.ResourceResolutionException;
import com.rackspace.papi.commons.config.resource.impl.BufferedURLConfigurationResource;
import com.rackspace.papi.commons.util.StringUtilities;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author zinic
 */
public class ConfigurationResolver implements ConfigurationResourceResolver {

   private static final String FILE_RESOURCE_SCHEME = "file:", CLASSPATH_RESOURCE_SCHEME = "classpath:";
   private final String fileSchemeRootDirectory;

   public ConfigurationResolver(String configurationRoot) {
      this.fileSchemeRootDirectory = preppendFileURISpec(configurationRoot);
   }

   final String preppendFileURISpec(String urn) {
      return !urn.startsWith(FILE_RESOURCE_SCHEME) ? FILE_RESOURCE_SCHEME + urn : urn;
   }

   @Override
   public ConfigurationResource resolve(String resourceName) throws ResourceResolutionException {
      if (resourceName.startsWith(FILE_RESOURCE_SCHEME)) {
         // Full file path
         try {
            return new BufferedURLConfigurationResource(new URL(resourceName));
         } catch (MalformedURLException murle) {
            throw new ResourceResolutionException("Unable to build URL for resource. Resource: "
                    + resourceName + ". Reason: " + murle.getMessage(), murle);
         }
      } else if (resourceName.startsWith(CLASSPATH_RESOURCE_SCHEME)) {
         // Classpath file path
         return new ClasspathConfigurationResource(resourceName.substring(10));
      } else {
         final String location = StringUtilities.join(fileSchemeRootDirectory, "/", resourceName);

         try {
            return new BufferedURLConfigurationResource(new URL(location));
         } catch (MalformedURLException murle) {
            throw new ResourceResolutionException("Unable to build URL for resource. Resource: "
                    + location + ". Reason: " + murle.getMessage(), murle);
         }
      }
   }
}
