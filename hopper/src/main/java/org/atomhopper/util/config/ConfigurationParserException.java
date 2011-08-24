/*
 *  Copyright 2010 Rackspace.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */
package net.jps.atom.hopper.util.config;

/**
 *
 *
 */
public class ConfigurationParserException extends RuntimeException {

    public ConfigurationParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationParserException(String message) {
        super(message);
    }
}
