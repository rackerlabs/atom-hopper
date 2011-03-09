/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.jetty;

import net.jps.atom.hopper.AtomHopperServlet;
import net.jps.atom.hopper.servlet.ServletInitParameter;
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

    public AtomHopperJettyServerBuilder(int portNumber) throws Exception {
        this.portNumber = portNumber;
    }

    private Server buildNewInstance() {
        final Server jettyServerReference = new Server(portNumber);
        final ServletContextHandler rootContext = buildRootContext(jettyServerReference);

        final ServletHolder atomHopServer = new ServletHolder(AtomHopperServlet.class);
        atomHopServer.setInitParameter(ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString(), null);
        atomHopServer.setInitParameter(ServletInitParameter.CONFIGURATION_DIRECTORY.toString(), null);

        rootContext.addServlet(atomHopServer, "/*");
        
        return jettyServerReference;
    }

    private ServletContextHandler buildRootContext(Server serverReference) {
        final ServletContextHandler servletContext = new ServletContextHandler(serverReference, "/");
        servletContext.getInitParams().put("contextConfigLocation", "classpath:/META-INF/application-context.xml");
        servletContext.addEventListener(new ContextLoaderListener());

        return servletContext;
    }

    public Server newServer() {
        return buildNewInstance();
    }
}
