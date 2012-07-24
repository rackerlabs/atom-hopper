package org.atomhopper;

import org.apache.abdera.Abdera;
import org.apache.abdera.ext.json.JSONFilter;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.abdera.WorkspaceProvider;
import org.atomhopper.config.process.AtomHopperConfigurationPreprocessor;
import org.atomhopper.config.process.WorkspaceConfigProcessor;
import org.atomhopper.config.v1_0.Configuration;
import org.atomhopper.config.v1_0.ConfigurationDefaults;
import org.atomhopper.config.v1_0.HostConfiguration;
import org.atomhopper.config.v1_0.WorkspaceConfiguration;
import org.atomhopper.exceptions.ContextAdapterResolutionException;
import org.atomhopper.servlet.ApplicationContextAdapter;
import org.atomhopper.servlet.DefaultEmptyContext;
import org.atomhopper.servlet.ServletInitParameter;
import org.atomhopper.util.config.ConfigurationParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;
import org.atomhopper.config.ConfigurationManager;
import org.atomhopper.servlet.context.AtomHopperContextParameterKeys;

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

    private ApplicationContextAdapter applicationContextAdapter;
    private Abdera abderaReference;
    private Configuration configuration;

    public AtomHopperServlet() {
    }

    @Override
    public void init() throws ServletException {
        abderaReference = getAbdera();

        final ConfigurationManager<Configuration> cfgManager = (ConfigurationManager<Configuration>)getServletContext().getAttribute(AtomHopperContextParameterKeys.CFG_MANAGER);
        configuration = cfgManager.getConfiguration();

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
    @Override
    protected Provider createProvider() {
        final WorkspaceProvider workspaceProvider = new WorkspaceProvider(getHostConfiguration());
        final String atomhopperUrlPattern = (getServletConfig().getInitParameter("atomhopper-url-pattern") == null) ?
                "/" : getServletConfig().getInitParameter("atomhopper-url-pattern"); 
        
        workspaceProvider.init(abderaReference, parseDefaults(configuration.getDefaults()));

        final AtomHopperConfigurationPreprocessor preprocessor = new AtomHopperConfigurationPreprocessor(configuration);
        configuration = preprocessor.applyDefaults().getConfiguration();

        ConfigurationDefaults configurationDefaults = configuration.getDefaults();
        workspaceProvider.init(abderaReference, parseDefaults(configurationDefaults));

        for (WorkspaceConfiguration workspaceCfg : configuration.getWorkspace()) {
            final WorkspaceConfigProcessor cfgProcessor = new WorkspaceConfigProcessor(
                    workspaceCfg, applicationContextAdapter,
                    workspaceProvider.getTargetResolver(), atomhopperUrlPattern);

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
