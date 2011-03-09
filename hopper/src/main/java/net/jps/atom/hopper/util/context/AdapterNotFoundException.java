/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
