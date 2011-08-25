package org.atomhopper.util.context;

/**
 *
 * 
 */
public class AdapterConstructionException extends RuntimeException {

    public AdapterConstructionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public AdapterConstructionException(String string) {
        super(string);
    }
}
