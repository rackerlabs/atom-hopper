package org.atomhopper;

import com.rackspace.papi.service.context.jndi.ContextAdapter;
import com.rackspace.papi.service.context.jndi.ServletContextHelper;
import org.apache.abdera.ext.json.JSONFilter;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.abdera.WorkspaceProvider;
import org.atomhopper.config.v1_0.Configuration;
import org.atomhopper.config.v1_0.ConfigurationDefaults;
import org.atomhopper.exceptions.ContextAdapterResolutionException;
import org.atomhopper.servlet.ApplicationContextAdapter;
import org.atomhopper.servlet.DefaultEmptyContext;
import org.atomhopper.servlet.ServletInitParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import org.atomhopper.abdera.ThreadSafeWorkspaceManager;
import org.atomhopper.config.AtomHopperConfigurationManager;
import org.atomhopper.repose.ReposeContextManager;

/**
 * This class is the entry point for the atom server application. This servlet
 * is responsible for setting up any required services as well as performing the
 * parsing of the atom server configuration. In addition, the servlet is also
 * responsible for context clean-up using the destroy method. This method should
 * make sure that any resources that have independent thread life-cycles are
 * correctly disposed of.
 */
public final class AtomHopperServlet extends AbderaServlet {

   private static final Logger LOG = LoggerFactory.getLogger(AtomHopperServlet.class);
   private static final String DEFAULT_CONFIGURATION_LOCATION = "/etc/atomhopper/atom-server.cfg.xml";
   private final ReposeContextManager reposeContextManager;
   private final WorkspaceProvider workspaceProvider;
   private AtomHopperConfigurationManager configurationManager;
   private ApplicationContextAdapter applicationContextAdapter;

   public AtomHopperServlet() {
      // The narwhal invades best at night, fueled by caffeine and insane j-pop
      reposeContextManager = new ReposeContextManager();
      
      // Init the persistent workspace provider
      // TODO:Review - this should be a sane, pre-startup default eventually
      workspaceProvider = new WorkspaceProvider(new ThreadSafeWorkspaceManager());
      workspaceProvider.init(getAbdera(), new HashMap<String, String>());
      workspaceProvider.addFilter(new JSONFilter());
   }

   @Override
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      
      applicationContextAdapter = getContextAdapter();
      applicationContextAdapter.usingServletContext(getServletContext());

      configurationManager = new AtomHopperConfigurationManager(applicationContextAdapter, DEFAULT_CONFIGURATION_LOCATION);
      
      // Init repose
      final ServletContextEvent servletContextEvent = new ServletContextEvent(config.getServletContext());
      reposeContextManager.contextInitialized(servletContextEvent);

      // Get the configuration service out of repose's jndi context
      final ContextAdapter reposeServiceContext = ServletContextHelper.getPowerApiContext(config.getServletContext());

      // Register for updates
      reposeServiceContext.configurationService().subscribeTo(getConfigurationLocation(), configurationManager, Configuration.class);
   }

   @Override
   public void destroy() {
      final ServletContextEvent servletContextEvent = new ServletContextEvent(getServletContext());
      reposeContextManager.contextDestroyed(servletContextEvent);

      super.destroy();
   }

   protected String getUrlPattern() {
      final String urlPattern = getServletConfig().getInitParameter("atomhopper-url-pattern");

      return urlPattern == null ? "/" : urlPattern;
   }

   protected ApplicationContextAdapter getContextAdapter() throws ContextAdapterResolutionException {
      String adapterClass = getInitParameter(ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString());

      // If no adapter class is set then use the default empty one
      if (StringUtils.isBlank(adapterClass)) {
         adapterClass = DefaultEmptyContext.class.getName();
      }

      try {
         final Object freshAdapter = Class.forName(adapterClass).newInstance();

         if (freshAdapter instanceof ApplicationContextAdapter) {
            return (ApplicationContextAdapter) freshAdapter;
         }
      } catch (Exception ex) {
         LOG.error(ex.getMessage(), ex);

         throw new ContextAdapterResolutionException(ex.getMessage(), ex);
      }

      throw new ContextAdapterResolutionException("Unknown application context adapter class: " + adapterClass);
   }

   protected String getConfigurationLocation() {
      final String configLocation = getInitParameter(ServletInitParameter.CONFIGURATION_LOCATION.toString());

      return !StringUtils.isBlank(configLocation) ? configLocation : DEFAULT_CONFIGURATION_LOCATION;
   }

   @Override
   protected Provider createProvider() {
      return workspaceProvider;
   }

   private Map<String, String> parseDefaults(ConfigurationDefaults defaults) {
      final Map<String, String> parameterMap = new HashMap<String, String>();

      if (defaults != null && defaults.getAuthor() != null && !StringUtils.isBlank(defaults.getAuthor().getName())) {
         parameterMap.put("author", defaults.getAuthor().getName());
      }

      return parameterMap;
   }
}
