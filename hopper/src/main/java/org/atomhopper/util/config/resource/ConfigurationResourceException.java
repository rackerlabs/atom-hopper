package org.atomhopper.util.config.resource;

/**
 *

 */
public class ConfigurationResourceException extends RuntimeException {

    public ConfigurationResourceException(Throwable thrwbl) {
        super(thrwbl);
    }

    public ConfigurationResourceException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public ConfigurationResourceException(String string) {
        super(string);
    }

    public ConfigurationResourceException() {
    }
}
