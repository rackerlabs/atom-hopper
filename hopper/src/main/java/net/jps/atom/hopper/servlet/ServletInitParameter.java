/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jps.atom.hopper.servlet;

/**
 *
 * 
 */
public enum ServletInitParameter {
    
    CONTEXT_ADAPTER_CLASS("context-adapter-class"),
    CONFIGURATION_DIRECTORY("config-directory");
    
    private final String value;
    
    private ServletInitParameter(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
