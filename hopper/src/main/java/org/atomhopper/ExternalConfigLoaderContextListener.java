package org.atomhopper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple utility listener to load certain properties before Spring Starts up.
 * 
 * This code is from https://bowerstudios.com/node/896
 */
public class ExternalConfigLoaderContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(ExternalConfigLoaderContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String configLocation = sce.getServletContext().getInitParameter("CONFIGDIR");
        if (configLocation == null) {
            configLocation = "/etc/atomhopper/";
        }

        try {
            new LogBackConfigLoader(configLocation + "logback.xml");
        } catch (Exception e) {
            logger.error("Unable to read config file", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
