package org.atomhopper;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.atomhopper.servlet.ServletInitParameter;
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
        try {
            InitialContext initialContext = new InitialContext();
            String configLocation = (String) initialContext.lookup("java:comp/env/logback/configuration-resource");
            LOGGER.info("Log file location : "+ configLocation);

            new LogBackConfigLoader(configLocation + "logback.xml");
        } catch (Exception e) {
            LOGGER.error("Unable to read config file", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
