package net.jps.atom.hopper;

import com.rackspace.cloud.commons.config.ConfigurationParserException;
import com.rackspace.cloud.commons.config.jaxb.JAXBConfigurationParser;
import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import net.jps.atom.hopper.exceptions.ServletInitException;
import net.jps.atom.hopper.abdera.WorkspaceProvider;
import net.jps.atom.hopper.archive.FeedArchivalService;
import net.jps.atom.hopper.archive.impl.QueuedFeedArchivalService;
import net.jps.atom.hopper.config.WorkspaceConfigProcessor;
import net.jps.atom.hopper.exceptions.ContextAdapterResolutionException;
import java.util.HashMap;
import javax.servlet.ServletException;
import net.jps.atom.hopper.config.v1_0.Configuration;
import net.jps.atom.hopper.config.v1_0.WorkspaceConfiguration;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;

public final class AtomHopperServlet extends AbderaServlet {

    private static final Logger log = new RCLogger(AtomHopperServlet.class);

    public static final String CONTEXT_ADAPTER_CLASS = "context-adapter-class";
    public static final String CONFIG_DIRECTORY = "sense-config-directory";
    public static final String DEFAULT_CONFIG_DIRECTORY = "/etc/rackspace-cloud/sense";

    private FeedArchivalService archivalService;
    private ApplicationContextAdapter applicationContextAdapter;
    private Abdera abderaObject;
    private Configuration configuration;

    @Override
    public void destroy() {
        archivalService.stopService();
        super.destroy();
    }

    @Override
    public void init() throws ServletException {
        abderaObject = getAbdera();

        archivalService = new QueuedFeedArchivalService();

        final String configLocation = getConfigDirectory() + "/sense.cfg.xml";

        try {
            log.info("Reading configuration file: " + configLocation);

            configuration = JAXBConfigurationParser.fromFile(configLocation, Configuration.class, net.jps.atom.hopper.config.v1_0.ObjectFactory.class).read();
        } catch (ConfigurationParserException cpe) {
            throw log.newException("Failed to read configuration file: " + configLocation, cpe, ServletInitException.class);
        }

        applicationContextAdapter = getContextAdapter();
        applicationContextAdapter.usingServletContext(getServletContext());

        archivalService.startService();

        super.init();
    }

    protected ApplicationContextAdapter getContextAdapter() throws ContextAdapterResolutionException {
        final String adapterClass = getInitParameter(CONTEXT_ADAPTER_CLASS);

        if (adapterClass == null || adapterClass.equals("")) {
            throw log.newException("Missing context adapter init-parameter for servlet: " + CONTEXT_ADAPTER_CLASS, ContextAdapterResolutionException.class);
        }

        try {
            final Object freshAdapter = Class.forName(adapterClass).newInstance();

            if (freshAdapter instanceof ApplicationContextAdapter) {
                return (ApplicationContextAdapter) freshAdapter;
            }
        } catch (Exception ex) {
            throw log.wrapError(ex, ContextAdapterResolutionException.class);
        }

        throw log.newException("Unknwon application context adapter class: " + adapterClass, ContextAdapterResolutionException.class);
    }

    protected String getConfigDirectory() {
        final String configDirectory = getInitParameter(CONFIG_DIRECTORY);

        return configDirectory == null || configDirectory.equals("") ? DEFAULT_CONFIG_DIRECTORY : configDirectory;
    }

    @Override
    protected Provider createProvider() {
        final WorkspaceProvider senseProvider = new WorkspaceProvider();

        //TODO: Provide property injection via config here
        senseProvider.init(abderaObject, new HashMap<String, String>());

        for (WorkspaceConfiguration workspaceCfg : configuration.getWorkspace()) {
            senseProvider.getWorkspaceManager().addWorkspace(
                    new WorkspaceConfigProcessor(workspaceCfg, applicationContextAdapter, abderaObject, archivalService).toHandler());
        }

        return senseProvider;
    }
}
