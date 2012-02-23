package org.atomhopper.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;

public class DefaultEmptyContext implements ApplicationContextAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultEmptyContext.class);
    
    @Override
    public <T> T fromContext(Class<T> classToCastTo) {
        return null;
    }

    @Override
    public <T> T fromContext(String refName, Class<T> classToCastTo) {
        return null;
    }

    @Override
    public void usingServletContext(ServletContext context) {
        LOG.warn("Missing context adapter init-parameter for servlet: " 
                + ServletInitParameter.CONTEXT_ADAPTER_CLASS.toString() 
                + " - Please note that this will enforce a default, empty context adapter for this servlet.");
    }
    
}
