package net.jps.atom.hopper.util.context;

/**
 *
 * 
 */
public class AdapterNotFoundException extends RuntimeException {

    public AdapterNotFoundException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public AdapterNotFoundException(String string) {
        super(string);
    }
}
