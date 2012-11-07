package org.atomhopper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple utility listener to load certain properties before Spring Starts up.
 * 
 * This code is modified from https://bowerstudios.com/node/896
 */
public class ExternalConfigLoaderContextListener implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalConfigLoaderContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        final String configLocation = "/etc/atomhopper/";

        try {
            new LogBackConfigLoader(configLocation + "logback.xml");
        } catch (Exception e) {
            LOGGER.error("Unable to read config file", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
