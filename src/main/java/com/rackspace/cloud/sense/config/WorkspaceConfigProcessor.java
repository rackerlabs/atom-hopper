package com.rackspace.cloud.sense.config;

import com.rackspace.cloud.sense.abdera.AbderaAdapterTools;
import com.rackspace.cloud.util.StringUtilities;
import com.rackspace.cloud.util.servlet.context.ApplicationContextAdapter;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import com.rackspace.cloud.sense.abdera.SenseFeedAdapter;
import com.rackspace.cloud.sense.abdera.TargetResolverField;
import com.rackspace.cloud.sense.client.adapter.AdapterTools;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.config.v1_0.FeedConfig;
import com.rackspace.cloud.sense.config.v1_0.WorkspaceConfig;
import com.rackspace.cloud.util.logging.Logger;
import com.rackspace.cloud.util.logging.RCLogger;
import com.rackspace.cloud.util.reflection.ReflectionTools;

import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;

import static com.rackspace.cloud.util.StringUtilities.*;

public class WorkspaceConfigProcessor {

    private static final Logger log = new RCLogger(WorkspaceConfigProcessor.class);
    private final ApplicationContextAdapter contextAdapter;
    private final WorkspaceConfig config;
    private final AdapterTools adapterTools;
    private FeedSourceAdapter defaultNamespaceAdapter;

    public WorkspaceConfigProcessor(WorkspaceConfig workspace, ApplicationContextAdapter contextAdapter, Abdera abderaReference) {
        this.config = workspace;
        this.contextAdapter = contextAdapter;

        adapterTools = new AbderaAdapterTools(abderaReference);
    }

    public WorkspaceHandler toHandler() {
        final List<SenseFeedAdapter> namespaceCollectionAdapters = new LinkedList<SenseFeedAdapter>();
        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();
        final TemplateTargetBuilder templateTargetBuilder = new TemplateTargetBuilder();

        final WorkspaceHandler workspace = new WorkspaceHandler(config, regexTargetResolver, templateTargetBuilder);

        defaultNamespaceAdapter = getFeedAdapterFromAppContext(config.getDefaultAdapterRef(), config.getDefaultAdapterClass());

        for (SenseFeedAdapter collectionAdapter : assembleServices(config.getFeed(), namespaceCollectionAdapters, regexTargetResolver, templateTargetBuilder)) {
            workspace.addCollectionAdapter(collectionAdapter);
        }

        return workspace;
    }

    private List<SenseFeedAdapter> assembleServices(List<FeedConfig> feedServices, List<SenseFeedAdapter> namespaceCollectionAdapters, RegexTargetResolver regexTargetResolver, TemplateTargetBuilder templateTargetBuilder) {
        final List<SenseFeedAdapter> collections = new LinkedList<SenseFeedAdapter>();

        final String namespace = StringUtilities.trim(config.getResourceBase(), "/");

        // service
        regexTargetResolver.setPattern(join("/(", namespace, ")/{0,1}(\\?[^#]*)?"),
                TargetType.TYPE_SERVICE,
                TargetResolverField.NAMESPACE.name());

        // categories
        regexTargetResolver.setPattern(join("/(", namespace, ")/{0,1}([^/#?]+);categories"),
                TargetType.TYPE_CATEGORIES,
                TargetResolverField.NAMESPACE.name(),
                TargetResolverField.CATEGORY.name());


        final String baseTemplate = "{target_base}/" + namespace;

        templateTargetBuilder.setTemplate(TargetType.TYPE_SERVICE, baseTemplate);
        templateTargetBuilder.setTemplate(TargetType.TYPE_COLLECTION, baseTemplate + "/{collection}{-opt|?|q,c,s,p,l,i,o}{-join|&|q,c,s,p,l,i,o}");
        templateTargetBuilder.setTemplate(TargetType.TYPE_CATEGORIES, baseTemplate + "/{collection};categories");
        templateTargetBuilder.setTemplate(TargetType.TYPE_ENTRY, baseTemplate + "/{collection}/{entry}");

        for (SenseFeedAdapter adapter : assembleFeedAdapters(feedServices, namespace, regexTargetResolver)) {
            collections.add(adapter);
            namespaceCollectionAdapters.add(adapter);
        }

        return collections;
    }

    private List<SenseFeedAdapter> assembleFeedAdapters(List<FeedConfig> feeds, String namespace, RegexTargetResolver regexTargetResolver) {
        final List<SenseFeedAdapter> collections = new LinkedList<SenseFeedAdapter>();

        for (FeedConfig feed : feeds) {
            final FeedSourceAdapter feedSource = getFeedSourceAdapter(feed);

            feedSource.setAdapterTools(adapterTools);

            final SenseFeedAdapter adapter = new SenseFeedAdapter(feed, feedSource);

            final String resource = StringUtilities.trim(feed.getResource(), "/");

            final String feedRegex = join("/(", namespace, ")/(", resource, ")/{0,1}(\\?[^#]*)?");
            final String entryRegex = join("/(", namespace, ")/(", resource, ")/([^/#?]+)(\\?[^#]*)?");

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

    private FeedSourceAdapter getFeedSourceAdapter(FeedConfig feed) {
        final FeedSourceAdapter feedSpecificAdapter = getFeedAdapterFromAppContext(feed.getAdapterRef(), feed.getAdapterClass());

        if (feedSpecificAdapter == null && defaultNamespaceAdapter == null) {
            throw new SenseConfigurationException("Failed to find or build an appropriate FeedSourceAdapter for feed: " + feed.getTitle());
        }

        return feedSpecificAdapter != null ? feedSpecificAdapter : defaultNamespaceAdapter;
    }

    private FeedSourceAdapter getFeedAdapterFromAppContext(String adapterReference, String className) {
        FeedSourceAdapter adapter = !isBlank(adapterReference) ? contextAdapter.fromContext(adapterReference, FeedSourceAdapter.class) : null;

        if (adapter != null && !isBlank(className)) {
            try {
                final Class datasourceAdapterClass = Class.forName(className);

                if (!FeedSourceAdapter.class.isAssignableFrom(datasourceAdapterClass)) {
                    throw new IllegalArgumentException("Class: " + datasourceAdapterClass.getCanonicalName() + " does not implement " + FeedSourceAdapter.class.getCanonicalName());
                }

                final FeedSourceAdapter contextSourcedAdapter = contextAdapter.fromContext((Class<FeedSourceAdapter>) datasourceAdapterClass);

                adapter = contextSourcedAdapter != null ? contextSourcedAdapter : (FeedSourceAdapter) ReflectionTools.construct(datasourceAdapterClass, new Object[0]);
            } catch (ClassNotFoundException classNotFoundException) {
                throw log.newException("Class: " + className + " can not be found. Please check your configuration.", classNotFoundException, SenseConfigurationException.class);
            } catch (Throwable t) {
                throw log.newException("Error occured while trying to build FeedSourceAdapters. Please check your configuration. Reason: " + t.getMessage(), SenseConfigurationException.class);
            }
        }

        return null;
    }
}
