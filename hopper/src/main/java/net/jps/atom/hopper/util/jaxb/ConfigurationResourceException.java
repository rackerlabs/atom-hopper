package net.jps.atom.hopper.util.jaxb;

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
