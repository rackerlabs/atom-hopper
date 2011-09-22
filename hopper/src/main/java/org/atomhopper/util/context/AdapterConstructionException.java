package org.atomhopper.util.context;

/**
 *
 * 
 */
class AdapterConstructionException extends RuntimeException {

    public AdapterConstructionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public AdapterConstructionException(String string) {
        super(string);
    }
}
