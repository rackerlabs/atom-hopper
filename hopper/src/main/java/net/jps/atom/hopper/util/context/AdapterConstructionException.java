/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.util.context;

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
