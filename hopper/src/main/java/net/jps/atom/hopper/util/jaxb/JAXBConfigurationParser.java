/*
 *  Copyright 2010 Rackspace.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */
package net.jps.atom.hopper.util.jaxb;

import com.rackspace.cloud.commons.config.ConfigurationParserException;
import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import java.net.URI;
import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import net.jps.atom.hopper.util.uri.CustomSchemeResolver;
import net.jps.atom.hopper.util.uri.UriToUrlResolver;

/**
 *
 *
 */
public final class JAXBConfigurationParser<T> {

    private static final Logger LOG = new RCLogger(JAXBConfigurationParser.class);

    private final URI confingurationResource;
    private final Class<T> configClass;
    private final JAXBContext jaxbContext;

    private UriToUrlResolver customUriSchemeResolver;

    public JAXBConfigurationParser(URI confingurationResource, Class<T> configClass, Class<?>... objectFactories) {
        try {
            jaxbContext = JAXBContext.newInstance(objectFactories);
        } catch (JAXBException jaxbex) {
            throw LOG.newException("Failed to create the JAXB context required for configuration marshalling", jaxbex, ConfigurationParserException.class);
        }

        this.configClass = configClass;
        this.confingurationResource = confingurationResource;

        customUriSchemeResolver = CustomSchemeResolver.newDefaultInstance();
    }

    public void setCustomUriSchemeResolver(UriToUrlResolver customUriSchemeResolver) {
        this.customUriSchemeResolver = customUriSchemeResolver;
    }

    public T read() {
        T rootConfigElement;

        try {
            final URL location = customUriSchemeResolver.toURL(confingurationResource);
            final Object unmarshaledObj = jaxbContext.createUnmarshaller().unmarshal(location.openStream());

            if (configClass.isInstance(unmarshaledObj)) {
                rootConfigElement = (T) unmarshaledObj;
            } else if (unmarshaledObj instanceof JAXBElement) {
                rootConfigElement = ((JAXBElement<T>) unmarshaledObj).getValue();
            } else {
                throw LOG.newException("Failed to read config", ConfigurationParserException.class);
            }
        } catch (Exception ex) {
            throw LOG.wrapError(ex, ConfigurationParserException.class);
        }

        return rootConfigElement;
    }
}
