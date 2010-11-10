/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.config;

/**
 *
 * @author zinic
 */
public class SenseConfigurationException extends RuntimeException {

    public SenseConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SenseConfigurationException(String message) {
        super(message);
    }
}
