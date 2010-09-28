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

import com.rackspace.cloud.sense.abdera.SenseFeedAdapter;
import com.rackspace.cloud.sense.abdera.TargetResolverField;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.context.ApplicationContextAdapter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.CollectionInfo;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;
import org.apache.abdera.protocol.server.impl.SimpleWorkspaceInfo;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;

import static com.rackspace.cloud.sense.util.Utilities.*;

/**
 *
 * @author John Hopper
 */
public class NamespaceConfigProcessor {

    private final SenseNamespaceConfiguration config;
    private final Abdera abderaReference;
    private final ApplicationContextAdapter contextAdapter;

    public NamespaceConfigProcessor(SenseNamespaceConfiguration config, ApplicationContextAdapter contextAdapter, Abdera abderaReference) {
        this.config = config;
        this.contextAdapter = contextAdapter;
        this.abderaReference = abderaReference;
    }

    public NamespaceConfig toConfig() {
        final SimpleWorkspaceInfo workspace = new SimpleWorkspaceInfo(config.getTitle());
        final List<SenseFeedAdapter> namespaceCollectionAdapters = new LinkedList<SenseFeedAdapter>();
        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();
        final TemplateTargetBuilder templateTargetBuilder = new TemplateTargetBuilder();

        for (CollectionInfo collectionAdapter : assembleServices(config.getAll(), namespaceCollectionAdapters, regexTargetResolver, templateTargetBuilder)) {
            workspace.addCollection(collectionAdapter);
        }

        return new NamespaceConfig(regexTargetResolver, templateTargetBuilder, namespaceCollectionAdapters, workspace);
    }

    private List<CollectionInfo> assembleServices(Collection<SenseServiceConfiguration> services, List<SenseFeedAdapter> namespaceCollectionAdapters, RegexTargetResolver regexTargetResolver, TemplateTargetBuilder templateTargetBuilder) {
        final List<CollectionInfo> collections = new LinkedList<CollectionInfo>();

        for (SenseServiceConfiguration service : services) {
            final String baseTemplate = "{target_base}" + service.getBaseUrn();

            templateTargetBuilder.setTemplate(TargetType.TYPE_SERVICE, baseTemplate);
            templateTargetBuilder.setTemplate(TargetType.TYPE_COLLECTION, baseTemplate + "/{collection}{-opt|?|q,c,s,p,l,i,o}{-join|&|q,c,s,p,l,i,o}");
            templateTargetBuilder.setTemplate(TargetType.TYPE_CATEGORIES, baseTemplate + "/{collection};categories");
            templateTargetBuilder.setTemplate(TargetType.TYPE_ENTRY, baseTemplate + "/{collection}/{entry}");

            // service
            regexTargetResolver.setPattern(join("(", service.getBaseUrn(), ")(\\?[^#]*)?"),
                    TargetType.TYPE_SERVICE,
                    TargetResolverField.NAMESPACE.name());

            // categories
            regexTargetResolver.setPattern(join("(", service.getBaseUrn(), ")([^/#?]+);categories"),
                    TargetType.TYPE_CATEGORIES,
                    TargetResolverField.NAMESPACE.name(),
                    TargetResolverField.CATEGORY.name());


            for (SenseFeedAdapter info : assembleFeedAdapters(service, regexTargetResolver)) {
                collections.add(info);
                namespaceCollectionAdapters.add(info);
            }
        }

        return collections;
    }

    private List<SenseFeedAdapter> assembleFeedAdapters(SenseServiceConfiguration service, RegexTargetResolver regexTargetResolver) {
        final List<SenseFeedAdapter> collections = new LinkedList<SenseFeedAdapter>();
        final FeedSourceAdapter datasource = getFeedAdapterFromAppContext(service);

        for (SenseFeedConfiguration feed : service.getAll()) {
            final SenseFeedAdapter adapter = new SenseFeedAdapter(abderaReference, feed, datasource);

            final String cleanFeedResource = feed.getResource().startsWith("/") ? feed.getResource().substring(1) : feed.getResource();

            final String feedRegex = join("(", service.getBaseUrn(), ")/(", cleanFeedResource, ")(\\?[^#]*)?");
            final String entryRegex = join("(", service.getBaseUrn(), ")/(", cleanFeedResource, ")/([^/#?]+)*(\\?[^#]*)?");

            adapter.addTargetRegex(feedRegex);
            adapter.addTargetRegex(entryRegex);

            // feed
            regexTargetResolver.setPattern(feedRegex,
                    TargetType.TYPE_COLLECTION,
                    TargetResolverField.NAMESPACE.name(),
                    TargetResolverField.FEED.name());
            // entry
            regexTargetResolver.setPattern(entryRegex,
                    TargetType.TYPE_ENTRY,
                    TargetResolverField.NAMESPACE.name(),
                    TargetResolverField.FEED.name(),
                    TargetResolverField.ENTRY.name());

            collections.add(adapter);
        }

        return collections;
    }

    private FeedSourceAdapter getFeedAdapterFromAppContext(SenseServiceConfiguration service) {
        FeedSourceAdapter adapter = null;

        if (stringIsBlank(service.getAdapterRef()) && stringIsBlank(service.getAdapterClass())) {
            throw new IllegalArgumentException("Service: " + service.getTitle() + " is missing both a datasource adapter reference and classname");
        }

        if (stringIsBlank(service.getAdapterRef())) {
            try {
                final Class datasourceAdapterClass = Class.forName(service.getAdapterClass());

                if (!FeedSourceAdapter.class.isAssignableFrom(datasourceAdapterClass)) {
                    throw new IllegalArgumentException("Class: " + datasourceAdapterClass.getCanonicalName() + " does not implement " + FeedSourceAdapter.class.getCanonicalName());
                }

                adapter = contextAdapter.fromContext((Class<FeedSourceAdapter>) datasourceAdapterClass);
            } catch (ClassNotFoundException classNotFoundException) {
                throw new IllegalArgumentException("Class: " + service.getAdapterClass() + " can not be found", classNotFoundException);
            }
        } else {
            adapter = contextAdapter.fromContext(service.getAdapterRef(), FeedSourceAdapter.class);
        }

        return adapter;
    }
}
