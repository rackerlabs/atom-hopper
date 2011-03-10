package net.jps.atom.hopper.util.config;

import net.jps.atom.hopper.util.config.resource.ConfigurationResource;

public interface ConfigurationParser<T> {

    T read();

    Class<T> getConfigurationClass();

    void setConfigurationResource(ConfigurationResource resource);

    ConfigurationResource getConfigurationResource();
}
