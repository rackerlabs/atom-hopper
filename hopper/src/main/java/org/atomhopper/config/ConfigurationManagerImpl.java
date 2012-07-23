package org.atomhopper.config;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import com.rackspace.papi.service.config.ConfigurationService;
import org.atomhopper.config.v1_0.Configuration;

/**
 *
 * @author zinic
 */
public class ConfigurationManagerImpl implements ConfigurationManager<Configuration> {

   private final UpdateListener<Configuration> configurationUpdateListener = new UpdateListener<Configuration>() {
      @Override
      public void configurationUpdated(Configuration t) {
         // More complex logic can go here - it might be a good idea to turn this into a full fledged class instead of an inner-class

         configuration = t;
      }
   };
   
   private Configuration configuration;

   public ConfigurationManagerImpl(ConfigurationService configurationService, String configurationLocation) {
      this.configuration = null;

      configurationService.subscribeTo(configurationLocation, configurationUpdateListener, Configuration.class);
   }

   @Override
   public Configuration getConfiguration() {
      return configuration;
   }

   @Override
   public boolean isConfigured() {
      return configuration != null;
   }
}
