package org.atomhopper.util.config;

import org.atomhopper.util.config.resource.ConfigurationResource;

public interface ConfigurationParser<T> {

    T read();

    Class<T> getConfigurationClass();

    void setConfigurationResource(ConfigurationResource resource);

    ConfigurationResource getConfigurationResource();
}
