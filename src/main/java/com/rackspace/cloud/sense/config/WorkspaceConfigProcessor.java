package com.rackspace.cloud.sense.config;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.reflection.ReflectionTools;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import com.rackspace.cloud.sense.client.adapter.SenseAdapterTools;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;
import com.rackspace.cloud.sense.abdera.SenseFeedAdapter;
import com.rackspace.cloud.sense.abdera.TargetResolverField;
import com.rackspace.cloud.sense.archive.FeedArchivalService;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;
import com.rackspace.cloud.sense.config.v1_0.ArchiveMarker;
import com.rackspace.cloud.sense.config.v1_0.FeedConfig;
import com.rackspace.cloud.sense.config.v1_0.WorkspaceConfig;

import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;

public class WorkspaceConfigProcessor {

    private static final Logger LOG = new RCLogger(WorkspaceConfigProcessor.class);
    
    public static final long DEFAULT_ARCHIVAL_INTERVAL = 3600000;

    private final FeedArchivalService feedArchivalService;
    private final ApplicationContextAdapter contextAdapter;
    private final WorkspaceConfig config;
    private final Abdera abderaReference;

    private FeedArchiver defaultArchiver;
    private FeedSourceAdapter defaultNamespaceAdapter;

    public WorkspaceConfigProcessor(WorkspaceConfig workspace, ApplicationContextAdapter contextAdapter, Abdera abderaReference, FeedArchivalService feedArchivalService) {
        this.config = workspace;
        this.contextAdapter = contextAdapter;
        this.feedArchivalService = feedArchivalService;
        this.abderaReference = abderaReference;
    }

    public WorkspaceHandler toHandler() {
        final List<SenseFeedAdapter> namespaceCollectionAdapters = new LinkedList<SenseFeedAdapter>();
        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();
        final TemplateTargetBuilder templateTargetBuilder = new TemplateTargetBuilder();

        final WorkspaceHandler workspace = new WorkspaceHandler(config, regexTargetResolver, templateTargetBuilder);

        defaultNamespaceAdapter = getFromAppContext(config.getDefaultAdapterRef(), config.getDefaultAdapterClass(), FeedSourceAdapter.class);
        defaultArchiver = getWorkspaceFeedArchiver(config);

        for (SenseFeedAdapter collectionAdapter : assembleServices(config.getFeed(), namespaceCollectionAdapters, regexTargetResolver, templateTargetBuilder)) {
            workspace.addCollectionAdapter(collectionAdapter);
        }

        return workspace;
    }

    private List<SenseFeedAdapter> assembleServices(List<FeedConfig> feedServices, List<SenseFeedAdapter> namespaceCollectionAdapters, RegexTargetResolver regexTargetResolver, TemplateTargetBuilder templateTargetBuilder) {
        final List<SenseFeedAdapter> collections = new LinkedList<SenseFeedAdapter>();

        final String namespace = StringUtilities.trim(config.getResourceBase(), "/");

        // service
        regexTargetResolver.setPattern(StringUtilities.join("/(", namespace, ")/{0,1}(\\?[^#]*)?"),
                TargetType.TYPE_SERVICE,
                TargetResolverField.NAMESPACE.name());

        // categories
        regexTargetResolver.setPattern(StringUtilities.join("/(", namespace, ")/{0,1}([^/#?]+);categories"),
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

            feedSource.setAdapterTools(new SenseAdapterTools(abderaReference));

            final SenseFeedAdapter adapter = new SenseFeedAdapter(feed, feedSource);
            final String resource = StringUtilities.trim(feed.getResource(), "/");

            final String feedRegex = StringUtilities.join("/(", namespace, ")/(", resource, ")/{0,1}(\\?[^#]*)?");
            final String entryRegex = StringUtilities.join("/(", namespace, ")/(", resource, ")/([^/#?]+)(\\?[^#]*)?");

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

            final ArchiveMarker archivalElement = feed.getArchive();

            if (archivalElement != null) {
                final FeedArchiver archiver = getFeedArchiver(archivalElement);

                try {
                    archiver.setArchivalInterval(archivalElement.getArchivalInterval());
                } catch (UnsupportedOperationException uoe) {
                    LOG.warn("Archiver class: "
                            + archiver.getClass().getName()
                            + " does not support time interval setting.", uoe);
                }


                feedArchivalService.registerArchiver(archiver);
            }
        }

        return collections;
    }

    private FeedArchiver getFeedArchiver(ArchiveMarker archiveMarker) {
        if (!StringUtilities.isBlank(archiveMarker.getArchiverClass()) || !StringUtilities.isBlank(archiveMarker.getArchiverRef())) {
            return getFromAppContext(archiveMarker.getArchiverRef(), archiveMarker.getArchiverClass(), FeedArchiver.class);
        }

        return defaultArchiver;
    }

    private FeedArchiver getWorkspaceFeedArchiver(WorkspaceConfig workspace) {
        final ArchiveMarker archiveConfig = workspace.getArchive();

        if (archiveConfig != null) {
            if (!StringUtilities.isBlank(archiveConfig.getArchiverClass())) {
                return getFromAppContext(archiveConfig.getArchiverRef(), archiveConfig.getArchiverClass(), FeedArchiver.class);
            } else if (StringUtilities.isBlank(archiveConfig.getArchiverRef())) {
//                return new FileSystemFeedArchiver(); //TODO: Add dir configuration for this
            }
        }

        return null;
    }

    private FeedSourceAdapter getFeedSourceAdapter(FeedConfig feed) {
        final FeedSourceAdapter feedSpecificAdapter = getFromAppContext(feed.getAdapterRef(), feed.getAdapterClass(), FeedSourceAdapter.class);

        if (feedSpecificAdapter == null && defaultNamespaceAdapter == null) {
            throw new SenseConfigurationException("Failed to find or build an appropriate FeedSourceAdapter for feed: " + feed.getTitle());
        }

        return feedSpecificAdapter != null ? feedSpecificAdapter : defaultNamespaceAdapter;
    }

    private <T> T getFromAppContext(String referenceName, String absoluteClassName, Class<T> parentClassDefinition) {
        T objectFromContext = !StringUtilities.isBlank(referenceName) ? contextAdapter.fromContext(referenceName, parentClassDefinition) : null;

        if (objectFromContext == null && !StringUtilities.isBlank(absoluteClassName)) {
            try {
                final Class<?> configuredClass = Class.forName(absoluteClassName);

                if (!configuredClass.isAssignableFrom(parentClassDefinition)) {
                    throw new IllegalArgumentException("Class: " + configuredClass.getCanonicalName() + " does not implement or extend " + parentClassDefinition.getCanonicalName());
                }

                final T instance = contextAdapter.fromContext((Class<? extends T>) configuredClass);

                objectFromContext = instance != null ? instance : (T) ReflectionTools.construct(configuredClass, new Object[0]);
            } catch (ClassNotFoundException cnfe) {
                throw LOG.newException("Class: " + absoluteClassName + " can not be found. Please check your configuration.", cnfe, SenseConfigurationException.class);
            } catch (Exception ex) {
                throw LOG.newException("Error occured while trying to source class information. Please check your configuration. Reason: " + ex.getMessage(), SenseConfigurationException.class);
            }
        }


        return objectFromContext;
    }
}
