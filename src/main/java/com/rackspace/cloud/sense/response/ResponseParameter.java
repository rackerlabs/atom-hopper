/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.response;

/**
 *
 * @author zinic
 */
public enum ResponseParameter {
    ENTRY_ID,
    MARKER,
    PAGE;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
