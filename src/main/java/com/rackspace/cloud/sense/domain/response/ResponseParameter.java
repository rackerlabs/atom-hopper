/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.domain.response;

/**
 *
 * @author zinic
 */
public enum ResponseParameter {

    MARKER,
    PAGE;

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
