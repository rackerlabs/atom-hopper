package net.jps.atom.hopper.config;

import net.jps.atom.hopper.abdera.WorkspaceHandler;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import net.jps.atom.hopper.abdera.FeedAdapter;
import net.jps.atom.hopper.abdera.TargetResolverField;
import net.jps.atom.hopper.archive.FeedArchivalService;
import net.jps.atom.hopper.adapter.archive.FeedArchiveSource;

import java.util.LinkedList;
import java.util.List;
import net.jps.atom.hopper.abdera.ArchiveAdapter;
import net.jps.atom.hopper.abdera.TargetAwareAbstractCollectionAdapter;
import net.jps.atom.hopper.adapter.FeedPublisher;
import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.adapter.archive.FeedArchiver;
import net.jps.atom.hopper.config.v1_0.AdapterDescriptor;
import net.jps.atom.hopper.config.v1_0.ArchivalConfiguration;
import net.jps.atom.hopper.config.v1_0.FeedConfiguration;
import net.jps.atom.hopper.config.v1_0.WorkspaceConfiguration;
import net.jps.atom.hopper.util.TargetRegexBuilder;
import net.jps.atom.hopper.util.context.AdapterGetter;

import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;

/**
 * TODO: Sanitize configured workspace and feed resource paths for regex insertion
 * 
 * @author zinic
 */
public class WorkspaceConfigProcessor {

    private static final Logger LOG = new RCLogger(WorkspaceConfigProcessor.class);
    public static final long HOUR_IN_MILLISECONDS = 3600000;
    private final FeedArchivalService feedArchivalService;
    private final AdapterGetter adapterGetter;
    private final WorkspaceConfiguration config;
    private final TargetRegexBuilder workspaceTarget;
    private FeedArchiveSource defaultArchiver;
    private FeedSource defaultFeedSource;

    //TODO: Consider builder pattern
    public WorkspaceConfigProcessor(WorkspaceConfiguration config, ApplicationContextAdapter contextAdapter, FeedArchivalService feedArchivalService, String contextPath) {
        this.config = config;
        this.adapterGetter = new AdapterGetter(contextAdapter);
        this.feedArchivalService = feedArchivalService;
        
        workspaceTarget = new TargetRegexBuilder();
        
        if (!StringUtilities.isBlank(contextPath)) {
            workspaceTarget.setContextPath(contextPath);
        }
    }

    public WorkspaceHandler toHandler() {
        final List<TargetAwareAbstractCollectionAdapter> namespaceCollectionAdapters = new LinkedList<TargetAwareAbstractCollectionAdapter>();
        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();

        final WorkspaceHandler workspace = new WorkspaceHandler(config, regexTargetResolver);

//        setDefaults(config);

        for (TargetAwareAbstractCollectionAdapter collectionAdapter : assembleServices(config.getFeed(), namespaceCollectionAdapters, regexTargetResolver)) {
            workspace.addCollectionAdapter(collectionAdapter);
        }

        return workspace;
    }

//    private void setDefaults(WorkspaceConfiguration workspaceConfig) {
//        if (!StringUtilities.isBlank(workspaceConfig.getDefaultAdapterRef()) || !StringUtilities.isBlank(workspaceConfig.getDefaultAdapterClass())) {
//            defaultFeedSource = getFeedSource(workspaceConfig.getDefaultAdapterRef(), workspaceConfig.getDefaultAdapterClass());
//        }
//
//        if (workspaceConfig.getArchive() != null) {
//            final ArchiveConfiguration archiveDefault = workspaceConfig.getArchive();
//
//            defaultArchiver = getArchiveAdapter(archiveDefault.getArchiverRef(), archiveDefault.getArchiverClass());
//        }
//    }
    private List<TargetAwareAbstractCollectionAdapter> assembleServices(List<FeedConfiguration> feedServices, List<TargetAwareAbstractCollectionAdapter> namespaceCollectionAdapters, RegexTargetResolver regexTargetResolver) {
        final List<TargetAwareAbstractCollectionAdapter> collections = new LinkedList<TargetAwareAbstractCollectionAdapter>();

        final String workspaceName = StringUtilities.trim(config.getResource(), "/");

        workspaceTarget.setWorkspace(workspaceName);

        // service
        regexTargetResolver.setPattern(workspaceTarget.toWorkspacePattern(),
                TargetType.TYPE_SERVICE,
                TargetResolverField.WORKSPACE.name());

        // categories
        regexTargetResolver.setPattern(workspaceTarget.toCategoryPattern(),
                TargetType.TYPE_CATEGORIES,
                TargetResolverField.WORKSPACE.name());

        for (TargetAwareAbstractCollectionAdapter adapter : assembleFeedAdapters(workspaceTarget, feedServices, regexTargetResolver)) {
            collections.add(adapter);
            namespaceCollectionAdapters.add(adapter);
        }

        return collections;
    }

    public <T> T getFromApplicationContext(String referenceName, String className, Class<T> expectedClass) {
        T resolvedReference = null;

        if (!StringUtilities.isBlank(referenceName)) {
            resolvedReference = adapterGetter.getByName(referenceName, expectedClass);
        }

        if (resolvedReference == null && !StringUtilities.isBlank(className)) {
            try {
                resolvedReference = adapterGetter.getByClassDefinition(Class.forName(className), expectedClass);
            } catch (ClassNotFoundException cnfe) {
                throw LOG.newException("Unable to find specified default adapter class: " + className, cnfe, ConfigurationException.class);
            }
        }

        return resolvedReference;
    }

    public <T> T getAdapter(AdapterDescriptor descriptor, Class<T> expectedClass, T defaultReturn) {
        final T adapter = descriptor != null
                ? getFromApplicationContext(descriptor.getReference(), descriptor.getClazz(), expectedClass)
                : null;

        return adapter != null ? adapter : defaultReturn;
    }

    private List<TargetAwareAbstractCollectionAdapter> assembleFeedAdapters(TargetRegexBuilder workspaceTarget, List<FeedConfiguration> feeds, RegexTargetResolver regexTargetResolver) {
        final List<TargetAwareAbstractCollectionAdapter> collections = new LinkedList<TargetAwareAbstractCollectionAdapter>();

        for (FeedConfiguration feed : feeds) {
            final FeedSource feedSource = getAdapter(feed.getFeedSource(), FeedSource.class, defaultFeedSource);
            final FeedPublisher feedPublisher = getAdapter(feed.getFeedPublisher(), FeedPublisher.class, null);

            final FeedAdapter feedAdapter = new FeedAdapter(feed, feedSource, feedPublisher);
            final String feedResource = StringUtilities.trim(feed.getResource(), "/");

            final TargetRegexBuilder feedTargetRegexBuilder = new TargetRegexBuilder(workspaceTarget);
            feedTargetRegexBuilder.setFeed(feedResource);

            final String feedRegex = feedTargetRegexBuilder.toFeedPattern();
            final String entryRegex = feedTargetRegexBuilder.toEntryPattern();

            feedAdapter.addTargetRegex(feedRegex);
            feedAdapter.addTargetRegex(entryRegex);

            // feed regex matching
            regexTargetResolver.setPattern(feedRegex,
                    TargetType.TYPE_COLLECTION,
                    TargetResolverField.WORKSPACE.name(),
                    TargetResolverField.FEED.name());

            // entry regex matching
            regexTargetResolver.setPattern(entryRegex,
                    TargetType.TYPE_ENTRY,
                    TargetResolverField.WORKSPACE.name(),
                    TargetResolverField.FEED.name(),
                    TargetResolverField.ENTRY.name());

            //Should we enable the archiver for this service?
            if (feed.getArchive() != null) {
                readArchivalConfiguration(feed, feedSource, feedAdapter, feedTargetRegexBuilder, regexTargetResolver, collections);
            }

            collections.add(feedAdapter);
        }

        return collections;
    }

    private void readArchivalConfiguration(FeedConfiguration feed, FeedSource feedSource, FeedAdapter feedAdapter, TargetRegexBuilder feedTargetRegexBuilder, RegexTargetResolver regexTargetResolver, List<TargetAwareAbstractCollectionAdapter> collections) {
        final ArchivalConfiguration archivalConfig = feed.getArchive();

        final FeedArchiveSource archiveSource = getAdapter(archivalConfig.getFeedArchiveSource(), FeedArchiveSource.class, null);
        final FeedArchiver archiver = getAdapter(archivalConfig.getFeedArchiver(), FeedArchiver.class, null);

        if (archiver != null) {
            //TODO: Implements archivalConfig.getIntervalSpec();
            archiver.setArchivalIntervalSpec(3600000);
            feedArchivalService.registerArchiveTask(feedSource, archiver);
        }

        if (archiveSource != null) {
            final ArchiveAdapter archiveAdapter = new ArchiveAdapter(archiveSource, feedAdapter);

            final String archiveRegex = feedTargetRegexBuilder.toArchivePattern();
            archiveAdapter.addTargetRegex(archiveRegex);

            // archive
            regexTargetResolver.setPattern(archiveRegex,
                    TargetType.TYPE_COLLECTION,
                    TargetResolverField.WORKSPACE.name(),
                    TargetResolverField.FEED.name(),
                    TargetResolverField.ENTRY.name(),
                    TargetResolverField.ARCHIVE_YEAR.name(),
                    TargetResolverField.ARCHIVE_MONTH.name(),
                    TargetResolverField.ARCHIVE_DAY.name(),
                    TargetResolverField.ARCHIVE_TIME.name());

            collections.add(archiveAdapter);
        }
    }
}
