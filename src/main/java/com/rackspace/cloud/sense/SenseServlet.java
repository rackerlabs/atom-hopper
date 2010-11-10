package com.rackspace.cloud.sense;

import com.rackspace.cloud.util.servlet.context.ApplicationContextAdapter;
import com.rackspace.cloud.sense.config.v1_0.SenseConfig;
import com.rackspace.cloud.powerapi.config.ConfigurationParserException;
import com.rackspace.cloud.powerapi.config.jaxb.JAXBConfigurationParser;
import com.rackspace.cloud.sense.exceptions.ServletInitException;
import com.rackspace.cloud.sense.abdera.SenseWorkspaceProvider;
import com.rackspace.cloud.sense.config.WorkspaceConfigProcessor;
import com.rackspace.cloud.sense.config.v1_0.WorkspaceConfig;
import com.rackspace.cloud.sense.exceptions.ContextAdapterResolutionException;
import com.rackspace.cloud.util.logging.Logger;
import com.rackspace.cloud.util.logging.RCLogger;
import java.util.HashMap;
import javax.servlet.ServletException;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;

public final class SenseServlet extends AbderaServlet {

    private static final Logger log = new RCLogger(SenseServlet.class);

    public static final String CONTEXT_ADAPTER_CLASS = "context-adapter-class";
    public static final String CONFIG_DIRECTORY = "sense-config-directory";
    public static final String DEFAULT_CONFIG_DIRECTORY = "/etc/rackspace-cloud/sense";

    private ApplicationContextAdapter applicationContextAdapter;
    private Abdera abderaObject;
    private SenseConfig configuration;

    @Override
    public void init() throws ServletException {
        abderaObject = getAbdera();

        final String configLocation = getConfigDirectory() + "/sense.cfg.xml";

        try {
            log.info("Reading configuration file: " + configLocation);

            configuration = JAXBConfigurationParser.fromFile(configLocation, SenseConfig.class, "com.rackspace.cloud.sense.config.v1_0").read();
        } catch (ConfigurationParserException cpe) {
            throw log.newException("Failed to read configuration file: " + configLocation, cpe, ServletInitException.class);
        }

        applicationContextAdapter = getContextAdapter();
        applicationContextAdapter.usingServletContext(getServletContext());

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
        final SenseWorkspaceProvider provider = new SenseWorkspaceProvider();

        //TODO: Provide property injection via config here
        provider.init(abderaObject, new HashMap<String, String>());

        for (WorkspaceConfig workspaceCfg : configuration.getWorkspace()) {
            provider.getWorkspaceManager().addWorkspace(
                    new WorkspaceConfigProcessor(workspaceCfg, applicationContextAdapter, abderaObject).toHandler());
        }

        return provider;
    }
}
