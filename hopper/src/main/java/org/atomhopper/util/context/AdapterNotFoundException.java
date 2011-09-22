package org.atomhopper.util.context;

/**
 *
 * 
 */
class AdapterNotFoundException extends RuntimeException {

    public AdapterNotFoundException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public AdapterNotFoundException(String string) {
        super(string);
    }
}
