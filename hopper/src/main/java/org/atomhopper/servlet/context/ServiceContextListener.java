package org.atomhopper.servlet.context;

import com.rackspace.papi.commons.config.manager.ConfigurationUpdateManager;
import com.rackspace.papi.commons.config.resource.ConfigurationResourceResolver;
import com.rackspace.papi.commons.config.resource.impl.DirectoryResourceResolver;
import com.rackspace.papi.commons.util.thread.DestroyableThreadWrapper;
import com.rackspace.papi.service.config.ConfigurationService;
import com.rackspace.papi.service.config.impl.PowerApiConfigurationManager;
import com.rackspace.papi.service.event.PowerProxyEventKernel;
import com.rackspace.papi.service.event.PowerProxyEventManager;
import com.rackspace.papi.service.event.common.EventService;
import com.rackspace.papi.service.threading.ThreadingService;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.config.ConfigurationConstants;
import org.atomhopper.config.ConfigurationResolver;
import org.atomhopper.config.EventedUpdateManager;
import org.atomhopper.servlet.ServletInitParameter;

/**
 *
 * @author zinic
 */
public class ServiceContextListener implements ServletContextListener {

   private static final ThreadingService THREADING_SERVICE = new ThreadingService() {
      @Override
      public Thread newThread(Runnable r, String name) {
         final Thread newThread = new Thread(r, name);

         return newThread;
      }
   };
   private ConfigurationUpdateManager updateManager;
   private ConfigurationService configurationService;
   private DestroyableThreadWrapper eventKernelThread;
   private PowerProxyEventKernel eventKernel;
   private EventService eventService;

   @Override
   public void contextInitialized(ServletContextEvent sce) {
      // Create the event service
      eventService = new PowerProxyEventManager();
      eventKernel = new PowerProxyEventKernel(eventService);

      eventKernelThread = new DestroyableThreadWrapper(THREADING_SERVICE.newThread(eventKernel, "Event Kernel Thread"), eventKernel);
      eventKernelThread.start();

      // Get the configuration directory
      final String configurationDirectory = sce.getServletContext().getInitParameter(ServletInitParameter.CONFIGURATION_DIRECTORY.toString());

      // Set our resolver to the configured directory
      final ConfigurationResourceResolver cfgResolver = StringUtils.isNotBlank(configurationDirectory)
              ? new ConfigurationResolver(configurationDirectory)
              : new ConfigurationResolver(ConfigurationConstants.DEFAULT_DIRECTORY);

      // Create our update manager
      final EventedUpdateManager configurationUpdateManager = new EventedUpdateManager(eventService);
      updateManager = configurationUpdateManager;

      // Create the configuration backend manager
      configurationService = new PowerApiConfigurationManager();

      configurationService.setUpdateManager(updateManager);
      configurationService.setResourceResolver(cfgResolver);

      // Lastly create our configuration manager
      sce.getServletContext().setAttribute(AtomHopperContextParameterKeys.CFG_SERVICE, configurationService);

      // Start the update manager and cross your fingers, yo
      configurationUpdateManager.start(THREADING_SERVICE);
   }

   @Override
   public void contextDestroyed(ServletContextEvent sce) {
      configurationService.destroy();
      updateManager.destroy();
      eventKernelThread.destroy();
   }
}
