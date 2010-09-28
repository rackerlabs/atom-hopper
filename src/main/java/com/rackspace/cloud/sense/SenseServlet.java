/*
 *  Copyright 2010 Rackspace.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense;

import com.rackspace.cloud.sense.exceptions.ServletInitException;
import com.rackspace.cloud.sense.config.SenseXmlConfiguration;
import com.rackspace.cloud.sense.config.SenseNamespaceConfiguration;
import com.rackspace.cloud.sense.context.ApplicationContextAdapter;
import com.rackspace.cloud.sense.abdera.SenseProvider;
import com.rackspace.cloud.sense.exceptions.ContextAdapterResolutionException;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.Provider;
import org.apache.abdera.protocol.server.servlet.AbderaServlet;

import static com.rackspace.cloud.sense.util.StaticLoggingFacade.*;

/**
 *
 * @author John Hopper
 */
public final class SenseServlet extends AbderaServlet {

    public static final String CONTEXT_ADAPTER_CLASS = "context-adapter-class";
    public static final String CONFIG_DIRECTORY = "sense-config-directory";
    public static final String DEFAULT_CONFIG_DIRECTORY = "/etc/rackspace-cloud/sense";

    private ApplicationContextAdapter applicationContextAdapter;
    private Abdera abderaObject;
    private SenseNamespaceConfiguration serverConfig;

    @Override
    public void init() throws ServletException {
        abderaObject = getAbdera();

        final String configuration = getConfigDirectory() + "/default.cfg.xml";

        try {
            logInfo("Reading configuration file: " + configuration);

            serverConfig = SenseXmlConfiguration.fromFile(configuration).toConfig(getServletContext().getContextPath());
        } catch (IOException ioe) {
            throw newException("Failed to read configuration file: " + configuration, ioe, ServletInitException.class);
        }

        applicationContextAdapter = getContextAdapter();
        applicationContextAdapter.usingServletContext(getServletContext());

        super.init();
    }

    protected ApplicationContextAdapter getContextAdapter() throws ContextAdapterResolutionException {
        final String adapterClass = getInitParameter(CONTEXT_ADAPTER_CLASS);

        if (adapterClass == null || adapterClass.equals("")) {
            throw newException("Missing context adapter init-parameter for servlet: " + CONTEXT_ADAPTER_CLASS, ContextAdapterResolutionException.class);
        }

        try {
            final Object freshAdapter = Class.forName(adapterClass).newInstance();

            if (freshAdapter instanceof ApplicationContextAdapter) {
                return (ApplicationContextAdapter) freshAdapter;
            }
        } catch (Exception ex) {
            throw wrapFatal(ex, ContextAdapterResolutionException.class);
        }

        throw newException("Unknwon application context adapter class: " + adapterClass, ContextAdapterResolutionException.class);
    }

    protected String getConfigDirectory() {
        final String configDirectory = getInitParameter(CONFIG_DIRECTORY);

        return configDirectory == null || configDirectory.equals("") ? DEFAULT_CONFIG_DIRECTORY : configDirectory;
    }

    @Override
    protected Provider createProvider() {
        return new SenseProvider(serverConfig, applicationContextAdapter, abderaObject);
    }
}
