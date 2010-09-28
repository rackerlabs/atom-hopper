/*
 *  Copyright 2010 Rackspace.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense.config;

import com.rackspace.cloud.sense.config.v1_0.Author;
import com.rackspace.cloud.sense.config.v1_0.Config;
import com.rackspace.cloud.sense.config.v1_0.Defaults;
import com.rackspace.cloud.sense.config.v1_0.Feed;
import com.rackspace.cloud.sense.config.v1_0.Namespace;
import com.rackspace.cloud.sense.config.v1_0.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.apache.log4j.Logger;

/**
 *
 * @author John Hopper
 */
public class SenseXmlConfiguration {

    private static final Logger LOG = Logger.getLogger(SenseXmlConfiguration.class);
    private final Config xmlConfig;
    private final Defaults defaultElements;

    public static SenseXmlConfiguration fromStream(InputStream stream) {
        return new SenseXmlConfiguration(stream);
    }

    public static SenseXmlConfiguration fromFile(String filename) throws IOException {
        final File f = new File(filename);

        if (!f.exists() || !f.canRead()) {
            //TODO: Throw exception
        }

        return new SenseXmlConfiguration(new FileInputStream(f));
    }

    private SenseXmlConfiguration(InputStream stream) {
        try {
            final JAXBContext context = JAXBContext.newInstance("com.rackspace.cloud.sense.config.v1_0");
            final Object unmarshaledObj = context.createUnmarshaller().unmarshal(stream);

            if (unmarshaledObj instanceof Config) {
                xmlConfig = (Config) unmarshaledObj;
            } else if (unmarshaledObj instanceof JAXBElement) {
                xmlConfig = ((JAXBElement<Config>) unmarshaledObj).getValue();
            } else {
                throw new RuntimeException("Failed to read config");
            }
        } catch (JAXBException jaxbe) {
            LOG.error(jaxbe.getMessage(), jaxbe);
            throw new RuntimeException("Lose");
        }

        defaultElements = xmlConfig.getDefaults();
    }

    private void set() {

    }

    public Config getRawConfig() {
        return xmlConfig;
    }

    public SenseNamespaceConfiguration toConfig(String servletContextPath) {
        final Namespace namespace = xmlConfig.getNamespace();

        final String baseUri = namespace.getUrl() + (namespace.getUrl().endsWith("/") ? "" : "/") + (servletContextPath.startsWith("/") ? servletContextPath.substring(1) : servletContextPath);
        final String namespaceResource = sanitizeResource(namespace.getResource());

        final SenseNamespaceConfiguration serverConfiguration = new SenseNamespaceConfiguration(baseUri, namespaceResource, namespace);

        marshalServices(namespace.getService(), namespaceResource, serverConfiguration);

        return serverConfiguration;
    }

    private void marshalServices(Collection<Service> services, String namespaceResource, SenseNamespaceConfiguration serverConfiguration) {
        for (Service service : services) {
            final String serviceResource = sanitizeResource(service.getResource());

            final String serviceUri = serverConfiguration.getFullUri() + serviceResource;
            final SenseServiceConfiguration serviceConfig = new SenseServiceConfiguration(serviceUri, namespaceResource + serviceResource, service);

            marshalFeeds(service.getFeed(), namespaceResource + serviceResource, serviceConfig);
            serverConfiguration.add(service.getTitle(), serviceConfig);
        }
    }

    private void marshalFeeds(Collection<Feed> feeds, String serviceResource, SenseServiceConfiguration serviceConfig) {
        for (Feed feed : feeds) {
            if (feed.getAuthor() == null || feed.getAuthor().getName() == null) {
                final Author defaultAuthor = new Author();
                defaultAuthor.setName(defaultElements.getAuthor().getName());

                feed.setAuthor(defaultAuthor);
            }

            final String feedResource = sanitizeResource(feed.getResource());

            final String feedUri = serviceConfig.getFullUri() + feedResource;
            serviceConfig.add(feed.getTitle(), new SenseFeedConfiguration(feedUri, serviceResource + feedResource, feed));
        }
    }

    /**
     * Returns an empty string if the resource is null or empty
     *
     * @param resource
     * @return
     */
    private String sanitizeResource(String resource) {
        if (resource == null || resource.equals("")) {
            return "";
        }

        //Strip the trailing slash if present
        final String firstTrim = resource.endsWith("/") ? resource.substring(0, resource.length() - 1) : resource;

        //Add prefix slash if not present
        return firstTrim.startsWith("/") ? firstTrim : "/" + firstTrim;
    }
}
