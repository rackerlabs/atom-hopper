/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.response;

/**
 *
 * 
 */
public enum ResponseParameter {
    PREVIOUS_MARKER,
    NEXT_MARKER;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
