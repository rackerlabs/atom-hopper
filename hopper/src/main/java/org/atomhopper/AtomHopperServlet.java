package org.atomhopper;

import org.atomhopper.abdera.WorkspaceProvider;
import org.atomhopper.config.WorkspaceConfigProcessor;
import org.atomhopper.config.v1_0.Configuration;
import org.atomhopper.config.v1_0.ConfigurationDefaults;
import org.atomhopper.config.v1_0.HostConfiguration;
import org.atomhopper.config.v1_0.WorkspaceConfiguration;
import org.atomhopper.exceptions.ContextAdapterResolutionException;
import org.atomhopper.exceptions.ServletInitException;
import org.atomhopper.servlet.ApplicationContextAdapter;
import org.atomhopper.servlet.ServletInitParameter;
import org.atomhopper.util.config.ConfigurationParser;
import org.atomhopper.util.config.ConfigurationParserException;
import org.atomhopper.util.config.jaxb.JAXBConfigurationParser;
import org.atomhopper.util.config.resource.uri.URIConfigurationResource;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.commons.lang.StringUtils;
import org.apache.abdera.ext.json.JSONFilter;

import javax.servlet.ServletException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import org.atomhopper.servlet.DefaultEmptyContext;
import org.atomhopper.util.config.resource.file.FileConfigurationResource;
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

    private static final String DEFAULT_CONFIGURATION_LOCATION = "/etc/atomhopper/atom-server.cfg.xml";

    private final ConfigurationParser<Configuration> configurationParser;
    private ApplicationContextAdapter applicationContextAdapter;
    private Abdera abderaReference;
    private Configuration configuration;

    public AtomHopperServlet() {
        //TODO: One day I'm going to integrate Power API's configuration framework into this but until this, this'll do
        configurationParser = new JAXBConfigurationParser<Configuration>(Configuration.class, org.atomhopper.config.v1_0.ObjectFactory.class);
    }

    @Override
    public void init() throws ServletException {
        abderaReference = getAbdera();

        final String configLocation = getConfigurationLocation();
        
        if (!(new File(configLocation).isFile())) {
            LOG.error("The atom-server.cfg.xml file doesn't exist");
        } else {
            LOG.info("Reading configuration: " + configLocation);
        }

        try {
            try {
                configurationParser.setConfigurationResource(new URIConfigurationResource(new URI(configLocation)));
            } catch (URISyntaxException ex) {
                configurationParser.setConfigurationResource(new FileConfigurationResource(configLocation));
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
        final WorkspaceProvider workspaceProvider = new WorkspaceProvider(getHostConfiguration());
        workspaceProvider.init(abderaReference, parseDefaults(configuration.getDefaults()));

        for (WorkspaceConfiguration workspaceCfg : configuration.getWorkspace()) {
            final WorkspaceConfigProcessor cfgProcessor = new WorkspaceConfigProcessor(
                    workspaceCfg, applicationContextAdapter,
                    workspaceProvider.getTargetResolver(), getServletContext().getContextPath());

            workspaceProvider.getWorkspaceManager().addWorkspaces(cfgProcessor.toHandler());
        }

        workspaceProvider.addFilter(new JSONFilter());

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

        if (defaults != null && defaults.getAuthor() != null && !StringUtils.isBlank(defaults.getAuthor().getName())) {
            parameterMap.put("author", defaults.getAuthor().getName());
        }

        return parameterMap;
    }
}
