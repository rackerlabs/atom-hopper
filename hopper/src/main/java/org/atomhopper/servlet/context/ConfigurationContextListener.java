package org.atomhopper.servlet.context;

import com.rackspace.papi.service.config.ConfigurationService;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.config.ConfigurationConstants;
import org.atomhopper.config.ConfigurationManagerImpl;
import org.atomhopper.servlet.ServletInitParameter;

/**
 *
 * @author zinic
 */
public class ConfigurationContextListener implements ServletContextListener {

   private ConfigurationManagerImpl configurationManagerImpl;

   @Override
   public void contextInitialized(ServletContextEvent sce) {
      final ServletContext servletContext = sce.getServletContext();
      // Get the default configuration directory
      final String configurationDirectory = sce.getServletContext().getInitParameter(ServletInitParameter.CONFIGURATION_LOCATION.toString());

      // Create the event service
      final ConfigurationService service = (ConfigurationService) servletContext.getAttribute(AtomHopperContextParameterKeys.CFG_SERVICE);

      // Set our resolver to the configured directory
      configurationManagerImpl = StringUtils.isNotBlank(configurationDirectory)
              ? new ConfigurationManagerImpl(service, configurationDirectory)
              : new ConfigurationManagerImpl(service, ConfigurationConstants.DEFAULT_CONFIGURATION);

      servletContext.setAttribute(AtomHopperContextParameterKeys.CFG_MANAGER, configurationManagerImpl);
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce) {
      configurationManagerImpl = null;
   }
}
