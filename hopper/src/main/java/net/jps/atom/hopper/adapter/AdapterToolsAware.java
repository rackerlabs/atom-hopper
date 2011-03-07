/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter;

/**
 *
 * @author zinic
 */
public interface AdapterToolsAware {

    /**
     * The system will inject adapters with a tools object using this method during
     * initialization of the service.
     *
     * @param tools
     */
    void setAdapterTools(AdapterTools tools);
}
