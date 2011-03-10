package net.jps.atom.hopper;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import java.net.URI;
import java.net.URISyntaxException;
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
import net.jps.atom.hopper.servlet.ServletInitParameter;
import net.jps.atom.hopper.util.config.ConfigurationParser;
import net.jps.atom.hopper.util.config.ConfigurationParserException;
import net.jps.atom.hopper.util.config.jaxb.JAXBConfigurationParser;
import net.jps.atom.hopper.util.config.resource.uri.URIConfigurationResource;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;

/**
 * This class is the entry point for the atom server application. This servlet is
 * responsible for setting up any required services as well as performing the
 * parsing of the atom server configuration. In addition, the servlet is also
 * responsible for context clean-up using the destroy method. This method should
 * make sure that any resources that have independent thread life-cycles are correctly
 * disposed of.
 */
public final class AtomHopperServlet extends AbderaServlet {

    private static final Logger LOG = new RCLogger(AtomHopperServlet.class);

    public static final String DEFAULT_CONFIGURATION_LOCATION = "/etc/atom-server/atom-server.cfg.xml";

    private final ConfigurationParser<Configuration> configurationParser;

    private FeedArchivalService archivalService;
    private ApplicationContextAdapter applicationContextAdapter;
    private Abdera abderaReference;
    private Configuration configuration;

    public AtomHopperServlet() {
        configurationParser = new JAXBConfigurationParser<Configuration>(Configuration.class, net.jps.atom.hopper.config.v1_0.ObjectFactory.class);
    }

    @Override
    public void destroy() {
        archivalService.stopService();
        super.destroy();
    }

    @Override
    public void init() throws ServletException {
        abderaReference = getAbdera();

        archivalService = new QueuedFeedArchivalService();

        final String configLocation = getConfigurationLocation();

        try {
            LOG.info("Reading configuration: " + configLocation);

            try {
                configurationParser.setConfigurationResource(new URIConfigurationResource(new URI(configLocation)));
            } catch (URISyntaxException ex) {
                //TODO: Should this be an error? Maybe we could have a fall back reader that tries file path by default
                throw new ServletInitException("Configuration location must be a URI. Consider using file:/absolute/path/to/file", ex);
            }

            configuration = configurationParser.read();
        } catch (ConfigurationParserException cpe) {
            throw LOG.newException("Failed to read configuration file: " + configLocation, cpe, ServletInitException.class);
        }

        applicationContextAdapter = getContextAdapter();
        applicationContextAdapter.usingServletContext(getServletContext());

        archivalService.startService();

        super.init();
    }

    protected ApplicationContextAdapter getContextAdapter() throws ContextAdapterResolutionException {
        final String adapterClass = getInitParameter(ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString());

        //TODO: Make this optional - use dummy adapter class for the cfg proc that returns null maybe
        if (StringUtilities.isBlank(adapterClass)) {
            throw LOG.newException("Missing context adapter init-parameter for servlet: " + ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString(), ContextAdapterResolutionException.class);
        }

        try {
            final Object freshAdapter = Class.forName(adapterClass).newInstance();

            if (freshAdapter instanceof ApplicationContextAdapter) {
                return (ApplicationContextAdapter) freshAdapter;
            }
        } catch (Exception ex) {
            throw LOG.wrapError(ex, ContextAdapterResolutionException.class);
        }

        throw LOG.newException("Unknwon application context adapter class: " + adapterClass, ContextAdapterResolutionException.class);
    }

    protected String getConfigurationLocation() {
        final String configLocation = getInitParameter(ServletInitParameter.CONFIGURATION_LOCATION.toString());

        return !StringUtilities.isBlank(configLocation) ? configLocation : DEFAULT_CONFIGURATION_LOCATION;
    }

    @Override
    protected Provider createProvider() {
        final WorkspaceProvider workspaceProvider = new WorkspaceProvider();

        //TODO: Provide property injection via config here
        workspaceProvider.init(abderaReference, new HashMap<String, String>());

        for (WorkspaceConfiguration workspaceCfg : configuration.getWorkspace()) {
            final WorkspaceConfigProcessor cfgProcessor = new WorkspaceConfigProcessor(
                    workspaceCfg, applicationContextAdapter, archivalService, getServletContext().getContextPath());

            workspaceProvider.getWorkspaceManager().addWorkspace(cfgProcessor.toHandler());
        }

        return workspaceProvider;
    }
}
