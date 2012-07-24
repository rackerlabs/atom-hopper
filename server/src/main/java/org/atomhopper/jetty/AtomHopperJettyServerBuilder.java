package org.atomhopper.jetty;


import org.atomhopper.AtomHopperServlet;
import org.atomhopper.servlet.ServletInitParameter;
import org.atomhopper.servlet.ServletSpringContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;


public class AtomHopperJettyServerBuilder {

    private final int portNumber;
    private String configurationPathAndFile = "";

    public AtomHopperJettyServerBuilder(int portNumber) {
        this.portNumber = portNumber;
    }
    
    public AtomHopperJettyServerBuilder(int portNumber, String configurationPathAndFile) {
        this.portNumber = portNumber;
        this.configurationPathAndFile = configurationPathAndFile;    
    }    

    private Server buildNewInstance() {
        final Server jettyServerReference = new Server(portNumber);
        final ServletContextHandler rootContext = buildRootContext(jettyServerReference);

        final ServletHolder atomHopServer = new ServletHolder(AtomHopperServlet.class);
        atomHopServer.setInitParameter(ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString(), ServletSpringContext.class.getName());
        if(configurationPathAndFile.length() <= 0) {
            atomHopServer.setInitParameter(ServletInitParameter.CONFIGURATION_LOCATION.toString(), "classpath:/META-INF/atom-server.cfg.xml");
        } else {
            atomHopServer.setInitParameter(ServletInitParameter.CONFIGURATION_LOCATION.toString(), configurationPathAndFile);
        }

        // If you want to use a different url pattern, try the following
        //atomHopServer.setInitParameter("atomhopper-url-pattern", "/mapping-example/");
        //rootContext.addServlet(atomHopServer, "/mapping-example/*");
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