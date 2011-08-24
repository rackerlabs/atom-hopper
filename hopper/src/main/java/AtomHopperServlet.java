package net.jps.atom.hopper;

import net.jps.atom.hopper.abdera.WorkspaceProvider;
import net.jps.atom.hopper.config.WorkspaceConfigProcessor;
import net.jps.atom.hopper.config.v1_0.Configuration;
import net.jps.atom.hopper.config.v1_0.ConfigurationDefaults;
import net.jps.atom.hopper.config.v1_0.HostConfiguration;
import net.jps.atom.hopper.config.v1_0.WorkspaceConfiguration;
import net.jps.atom.hopper.exceptions.ContextAdapterResolutionException;
import net.jps.atom.hopper.exceptions.ServletInitException;
import net.jps.atom.hopper.servlet.ApplicationContextAdapter;
import net.jps.atom.hopper.servlet.ServletInitParameter;
import net.jps.atom.hopper.util.config.ConfigurationParser;
import net.jps.atom.hopper.util.config.ConfigurationParserException;
import net.jps.atom.hopper.util.config.jaxb.JAXBConfigurationParser;
import net.jps.atom.hopper.util.config.resource.uri.URIConfigurationResource;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the entry point for the atom server application. This servlet is
 * responsible for setting up any required services as well as performing the
 * parsing of the atom server configuration. In addition, the servlet is also
 * responsible for context clean-up using the destroy method. This method should
 * make sure that any resources that have independent thread life-cycles are correctly
 * disposed of.
 */
public final class AtomHopperServlet extends AbderaServlet {

    private static final Logger LOG = LoggerFactory.getLogger(AtomHopperServlet.class);
    public static final String DEFAULT_CONFIGURATION_LOCATION = "/etc/atom-server/atom-server.cfg.xml";

    private final ConfigurationParser<Configuration> configurationParser;

    private ApplicationContextAdapter applicationContextAdapter;
    private Abdera abderaReference;
    private Configuration configuration;

    public AtomHopperServlet() {
        configurationParser = new JAXBConfigurationParser<Configuration>(Configuration.class, net.jps.atom.hopper.config.v1_0.ObjectFactory.class);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init() throws ServletException {
        abderaReference = getAbdera();

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
            LOG.error("Failed to read configuration file: " + configLocation, cpe);
            
            throw new ServletInitException(cpe.getMessage(), cpe);
        }

        applicationContextAdapter = getContextAdapter();
        applicationContextAdapter.usingServletContext(getServletContext());

        super.init();
    }

    protected ApplicationContextAdapter getContextAdapter() throws ContextAdapterResolutionException {
        final String adapterClass = getInitParameter(ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString());

        //TODO: Make this optional - use dummy adapter class for the cfg proc that returns null maybe
        if (StringUtils.isBlank(adapterClass)) {
            throw new ContextAdapterResolutionException("Missing context adapter init-parameter for servlet: " + ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString());
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
        final WorkspaceProvider workspaceProvider = new WorkspaceProvider(getHostConfiguration());
        workspaceProvider.init(abderaReference, parseDefaults(configuration.getDefaults()));

        for (WorkspaceConfiguration workspaceCfg : configuration.getWorkspace()) {
            final WorkspaceConfigProcessor cfgProcessor = new WorkspaceConfigProcessor(
                    workspaceCfg, applicationContextAdapter,
                    workspaceProvider.getTargetResolver(), getServletContext().getContextPath());

            workspaceProvider.getWorkspaceManager().addWorkspaces(cfgProcessor.toHandler());
        }

        return workspaceProvider;
    }

    private HostConfiguration getHostConfiguration() {
        //Initial parsing validation rules specify that there must always be a host configuration
        final HostConfiguration hostConfiguration = configuration.getHost();

        if (StringUtils.isBlank(hostConfiguration.getDomain())) {
            throw new ConfigurationParserException("No domain specified in the host configuration. This is required for link generation. Halting.");
        }

        return hostConfiguration;
    }

    private Map<String, String> parseDefaults(ConfigurationDefaults defaults) {
        final Map<String, String> parameterMap = new HashMap<String, String>();

        if (defaults != null) {
            if (defaults.getAuthor() != null && !StringUtils.isBlank(defaults.getAuthor().getName())) {
                parameterMap.put("author", defaults.getAuthor().getName());
            }
        }

        return parameterMap;
    }
}
