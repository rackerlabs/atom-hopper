package org.atomhopper.config;

import com.rackspace.papi.commons.config.manager.UpdateListener;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.abdera.ThreadSafeWorkspaceManager;
import org.atomhopper.abdera.WorkspaceProvider;
import org.atomhopper.config.v1_0.Configuration;
import org.atomhopper.config.v1_0.HostConfiguration;
import org.atomhopper.config.v1_0.WorkspaceConfiguration;
import org.atomhopper.servlet.ApplicationContextAdapter;
import org.atomhopper.util.config.ConfigurationParserException;

/**
 * This class represents the configuration read logic. Upon configuration update
 * the configurationUpdated method will be called.
 * 
 * @author zinic
 */
public class AtomHopperConfigurationManager implements UpdateListener<Configuration> {

   private final ApplicationContextAdapter applicationContextAdapter;
   private final String atomhopperUrlPattern;
   private WorkspaceProvider workspaceProvider;

   public AtomHopperConfigurationManager(ApplicationContextAdapter applicationContextAdapter, String atomhopperUrlPattern) {
      this.applicationContextAdapter = applicationContextAdapter;
      this.atomhopperUrlPattern = atomhopperUrlPattern;
   }

   public void setWorkspaceProvider(WorkspaceProvider workspaceProvider) {
      this.workspaceProvider = workspaceProvider;
   }

   @Override
   public synchronized void configurationUpdated(Configuration newConfiguration) {
      final AtomHopperConfigurationPreprocessor preprocessor = new AtomHopperConfigurationPreprocessor(newConfiguration);
      final Configuration configuration = preprocessor.applyDefaults().getConfiguration();

      // Update host config
      final HostConfiguration newHostConfig = configuration.getHost();

      if (StringUtils.isBlank(newHostConfig.getDomain())) {
         throw new ConfigurationParserException("No domain specified in the host configuration. This is required for link generation. Halting.");
      }

      workspaceProvider.setHostConfiguration(newHostConfig);
      
      // Update workspaces
      final ThreadSafeWorkspaceManager workspaceManager = workspaceProvider.getWorkspaceManager();
      workspaceManager.clearWorkspaces();
      
      for (WorkspaceConfiguration workspaceCfg : configuration.getWorkspace()) {
         final WorkspaceConfigProcessor cfgProcessor = new WorkspaceConfigProcessor(
                 workspaceCfg, applicationContextAdapter, workspaceProvider.getTargetResolver(), atomhopperUrlPattern);

         workspaceManager.addWorkspaces(cfgProcessor.toHandler());
      }
   }
}
