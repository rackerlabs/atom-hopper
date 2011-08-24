package net.jps.atom.hopper.util.config;

import net.jps.atom.hopper.util.config.resource.ConfigurationResource;

public abstract class AbstractConfigurationParser<T> implements ConfigurationParser<T> {

    private final Class<T> configurationClassDefinition;
    private ConfigurationResource configurationResource;

    public AbstractConfigurationParser(Class<T> configurationClassDefinition) {
        this.configurationClassDefinition = configurationClassDefinition;
    }

    @Override
    public Class<T> getConfigurationClass() {
        return configurationClassDefinition;
    }

    @Override
    public void setConfigurationResource(ConfigurationResource resource) {
        configurationResource = resource;
    }

    @Override
    public ConfigurationResource getConfigurationResource() {
        return configurationResource;
    }

    @Override
    public final T read() {
        if (configurationResource == null) {
            throw new IllegalStateException("A configuration resource must be set first before reading from it.");
        }

        return readConfiguration();
    }

    protected abstract T readConfiguration();
}
