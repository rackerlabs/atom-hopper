package org.atomhopper.jetty;

import org.atomhopper.AtomHopperLogCheckServlet;
import org.atomhopper.AtomHopperServlet;
import org.atomhopper.AtomHopperVersionServlet;
import org.atomhopper.servlet.ServletInitParameter;
import org.atomhopper.servlet.ServletSpringContext;
import org.atomhopper.servlet.context.ConfigurationContextListener;
import org.atomhopper.servlet.context.ServiceContextListener;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;

/**
 *
 *
 */
public class AtomHopperJettyServerBuilder {

   private final int portNumber;

   public AtomHopperJettyServerBuilder(int portNumber) {
      this.portNumber = portNumber;
   }

   private Server buildNewInstance() {
      final Server jettyServerReference = new Server(portNumber);
      final ServletContextHandler rootContext = buildRootContext(jettyServerReference);

      final ServletHolder atomHopServer = new ServletHolder(AtomHopperServlet.class);
      atomHopServer.setInitParameter(ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString(), ServletSpringContext.class.getName());
      
      final ServletHolder versionServlet = new ServletHolder(AtomHopperVersionServlet.class);
      final ServletHolder loggingTestServlet = new ServletHolder(AtomHopperLogCheckServlet.class);

      rootContext.addServlet(versionServlet, "/buildinfo");
      rootContext.addServlet(loggingTestServlet, "/logtest");
      rootContext.addServlet(atomHopServer, "/*");

      return jettyServerReference;
   }

   private ServletContextHandler buildRootContext(Server serverReference) {
      final ServletContextHandler servletContext = new ServletContextHandler(serverReference, "/");
      servletContext.setInitParameter("contextConfigLocation", "classpath:/META-INF/application-context.xml");
      servletContext.setInitParameter(ServletInitParameter.CONFIGURATION_LOCATION.toString(), "classpath:/META-INF/atom-server.cfg.xml");

      servletContext.addEventListener(new ContextLoaderListener());
      
      final ServiceContextListener serviceContextListener = new ServiceContextListener();
      servletContext.addEventListener(serviceContextListener);

      final ConfigurationContextListener contextListener = new ConfigurationContextListener();
      servletContext.addEventListener(contextListener);

      return servletContext;
   }

   public Server newServer() {
      return buildNewInstance();
   }
}
