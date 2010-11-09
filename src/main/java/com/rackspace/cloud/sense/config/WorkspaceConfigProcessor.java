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

import com.rackspace.cloud.util.StringUtilities;
import com.rackspace.cloud.util.servlet.context.ApplicationContextAdapter;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import com.rackspace.cloud.sense.abdera.SenseFeedAdapter;
import com.rackspace.cloud.sense.abdera.TargetResolverField;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.config.v1_0.FeedConfig;
import com.rackspace.cloud.sense.config.v1_0.WorkspaceConfig;

import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;

import static com.rackspace.cloud.util.StringUtilities.*;

/**
 *
 * @author John Hopper
 */
public class WorkspaceConfigProcessor {

    private final Abdera abderaReference;
    private final ApplicationContextAdapter contextAdapter;
    private final WorkspaceConfig config;

    public WorkspaceConfigProcessor(WorkspaceConfig workspace, ApplicationContextAdapter contextAdapter, Abdera abderaReference) {
        this.config = workspace;
        this.contextAdapter = contextAdapter;
        this.abderaReference = abderaReference;
    }

    public WorkspaceHandler toHandler() {
        final List<SenseFeedAdapter> namespaceCollectionAdapters = new LinkedList<SenseFeedAdapter>();
        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();
        final TemplateTargetBuilder templateTargetBuilder = new TemplateTargetBuilder();

        final WorkspaceHandler workspace = new WorkspaceHandler(config, regexTargetResolver, templateTargetBuilder);

        for (SenseFeedAdapter collectionAdapter : assembleServices(config.getFeed(), namespaceCollectionAdapters, regexTargetResolver)) {
            workspace.addCollectionAdapter(collectionAdapter);
        }

        return workspace;
    }

    private List<SenseFeedAdapter> assembleServices(List<FeedConfig> feedServices, List<SenseFeedAdapter> namespaceCollectionAdapters, RegexTargetResolver regexTargetResolver) {
        final List<SenseFeedAdapter> collections = new LinkedList<SenseFeedAdapter>();

        final String namespace = StringUtilities.trim(config.getResourceBase(), "/");

        // service
        regexTargetResolver.setPattern(join("/(", namespace, ")(\\?[^#]*)?"),
                TargetType.TYPE_SERVICE,
                TargetResolverField.NAMESPACE.name());

        // categories
        regexTargetResolver.setPattern(join("/(", namespace, ")([^/#?]+);categories"),
                TargetType.TYPE_CATEGORIES,
                TargetResolverField.NAMESPACE.name(),
                TargetResolverField.CATEGORY.name());

//            final String baseTemplate = "{target_base}" + resourceBase;
//            templateTargetBuilder.setTemplate(TargetType.TYPE_SERVICE, baseTemplate);
//            templateTargetBuilder.setTemplate(TargetType.TYPE_COLLECTION, baseTemplate + "/{collection}{-opt|?|q,c,s,p,l,i,o}{-join|&|q,c,s,p,l,i,o}");
//            templateTargetBuilder.setTemplate(TargetType.TYPE_CATEGORIES, baseTemplate + "/{collection};categories");
//            templateTargetBuilder.setTemplate(TargetType.TYPE_ENTRY, baseTemplate + "/{collection}/{entry}");

        for (SenseFeedAdapter info : assembleFeedAdapters(feedServices, namespace, regexTargetResolver)) {
            collections.add(info);
            namespaceCollectionAdapters.add(info);
        }

        return collections;
    }

    private List<SenseFeedAdapter> assembleFeedAdapters(List<FeedConfig> feeds, String namespace, RegexTargetResolver regexTargetResolver) {
        final List<SenseFeedAdapter> collections = new LinkedList<SenseFeedAdapter>();

        for (FeedConfig feed : feeds) {
            final FeedSourceAdapter feedSource = getFeedAdapterFromAppContext(feed);
            final SenseFeedAdapter adapter = new SenseFeedAdapter(abderaReference, feed, feedSource);

            final String resource = StringUtilities.trim(feed.getResource(), "/");

            final String feedRegex = join("/(", namespace, ")/(", resource, ")(\\?[^#]*)?");
            final String entryRegex = join("/(", namespace, ")/(", resource, ")/([^/#?]+)*(\\?[^#]*)?");

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

    //TODO: Handle workspace default adapter selection as well
    private FeedSourceAdapter getFeedAdapterFromAppContext(FeedConfig service) {
        FeedSourceAdapter adapter = null;

        if (isBlank(service.getAdapterRef()) && isBlank(service.getAdapterClass())) {
            throw new IllegalArgumentException("Service: " + service.getTitle() + " is missing both a datasource adapter reference and classname");
        }

        //TODO: Rethink this for class based instansiation
        if (isBlank(service.getAdapterRef())) {
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
