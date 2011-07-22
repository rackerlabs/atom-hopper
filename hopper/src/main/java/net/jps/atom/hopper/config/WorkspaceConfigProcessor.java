package net.jps.atom.hopper.config;

import net.jps.atom.hopper.abdera.ArchiveAdapter;
import net.jps.atom.hopper.abdera.FeedAdapter;
import net.jps.atom.hopper.abdera.TargetAwareAbstractCollectionAdapter;
import net.jps.atom.hopper.abdera.WorkspaceHandler;
import net.jps.atom.hopper.adapter.FeedPublisher;
import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.adapter.archive.FeedArchiveSource;
import net.jps.atom.hopper.adapter.archive.FeedArchiver;
import net.jps.atom.hopper.archive.FeedArchivalService;
import net.jps.atom.hopper.config.v1_0.AdapterDescriptor;
import net.jps.atom.hopper.config.v1_0.ArchivalConfiguration;
import net.jps.atom.hopper.config.v1_0.FeedConfiguration;
import net.jps.atom.hopper.config.v1_0.WorkspaceConfiguration;
import net.jps.atom.hopper.servlet.ApplicationContextAdapter;
import net.jps.atom.hopper.util.TargetRegexBuilder;
import net.jps.atom.hopper.util.context.AdapterGetter;
import net.jps.atom.hopper.util.log.Logger;
import net.jps.atom.hopper.util.log.RCLogger;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;
import org.apache.commons.lang.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * I eat configurations.
 *
 * TODO: Sanitize configured workspace and feed resource paths for regex insertion
 */
public class WorkspaceConfigProcessor {

    private static final Logger LOG = new RCLogger(WorkspaceConfigProcessor.class);
    private final RegexTargetResolver regexTargetResolver;
    private final FeedArchivalService feedArchivalService;
    private final AdapterGetter adapterGetter;
    private final WorkspaceConfiguration config;
    private final TargetRegexBuilder targetRegexGenerator;

    //TODO: Consider builder pattern
    public WorkspaceConfigProcessor(WorkspaceConfiguration config, ApplicationContextAdapter contextAdapter, FeedArchivalService feedArchivalService, RegexTargetResolver regexTargetResolver, String contextPath) {
        this.config = config;
        this.adapterGetter = new AdapterGetter(contextAdapter);
        this.feedArchivalService = feedArchivalService;
        this.regexTargetResolver = regexTargetResolver;

        targetRegexGenerator = new TargetRegexBuilder();

        if (!StringUtils.isBlank(contextPath)) {
            targetRegexGenerator.setContextPath(contextPath);
        }
    }

    public WorkspaceHandler toHandler() {
        final WorkspaceHandler workspace = new WorkspaceHandler(config);

        for (TargetAwareAbstractCollectionAdapter collectionAdapter : assembleFeeds(config.getFeed())) {
            workspace.addCollectionAdapter(collectionAdapter.getTarget(), collectionAdapter);
        }

        return workspace;
    }

    private List<TargetAwareAbstractCollectionAdapter> assembleFeeds(List<FeedConfiguration> feedServices) {
        final List<TargetAwareAbstractCollectionAdapter> collections = new LinkedList<TargetAwareAbstractCollectionAdapter>();

        final String workspaceName = StringUtils.strip(config.getResource(), "/");

        targetRegexGenerator.setWorkspace(workspaceName);

        // service
        regexTargetResolver.setPattern(targetRegexGenerator.toWorkspacePattern(),
                TargetType.TYPE_SERVICE,
                TargetRegexBuilder.getWorkspaceResolverFieldList());

        for (TargetAwareAbstractCollectionAdapter adapter : assembleFeedAdapters(targetRegexGenerator, feedServices)) {
            collections.add(adapter);
        }

        return collections;
    }

    public <T> T getFromApplicationContext(String referenceName, String className, Class<T> expectedClass) {
        T resolvedReference = null;

        if (!StringUtils.isBlank(referenceName)) {
            resolvedReference = adapterGetter.getByName(referenceName, expectedClass);
        }

        if (resolvedReference == null && !StringUtils.isBlank(className)) {
            try {
                resolvedReference = adapterGetter.getByClassDefinition(Class.forName(className), expectedClass);
            } catch (ClassNotFoundException cnfe) {
                throw LOG.newException("Unable to find specified default adapter class: " + className, cnfe, ConfigurationException.class);
            }
        }

        return resolvedReference;
    }

    public <T> T getAdapter(AdapterDescriptor descriptor, Class<T> expectedClass) {
        final T adapter = descriptor != null
                ? getFromApplicationContext(descriptor.getReference(), descriptor.getClazz(), expectedClass)
                : null;

        return adapter;
    }

    private List<TargetAwareAbstractCollectionAdapter> assembleFeedAdapters(TargetRegexBuilder workspaceTarget, List<FeedConfiguration> feeds) {
        final List<TargetAwareAbstractCollectionAdapter> collections = new LinkedList<TargetAwareAbstractCollectionAdapter>();

        for (FeedConfiguration feed : feeds) {
            final FeedSource feedSource = getAdapter(feed.getFeedSource(), FeedSource.class);
            final FeedPublisher feedPublisher = getAdapter(feed.getFeedPublisher(), FeedPublisher.class);

            final TargetRegexBuilder feedTargetRegexBuilder = new TargetRegexBuilder(workspaceTarget);
            feedTargetRegexBuilder.setFeed(feed.getResource());

            final FeedAdapter feedAdapter = new FeedAdapter(
                    feedTargetRegexBuilder.getFeedResource(), feed, feedSource, feedPublisher);

            // feed regex matching
            regexTargetResolver.setPattern(feedTargetRegexBuilder.toFeedPattern(),
                    TargetType.TYPE_COLLECTION,
                    TargetRegexBuilder.getFeedResolverFieldList());

            // feed categories
            regexTargetResolver.setPattern(feedTargetRegexBuilder.toCategoriesPattern(),
                    TargetType.TYPE_CATEGORIES,
                    TargetRegexBuilder.getCategoriesResolverFieldList());

            // entry regex matching
            regexTargetResolver.setPattern(feedTargetRegexBuilder.toEntryPattern(),
                    TargetType.TYPE_ENTRY,
                    TargetRegexBuilder.getEntryResolverFieldList());

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

        final FeedArchiveSource archiveSource = getAdapter(archivalConfig.getFeedArchiveSource(), FeedArchiveSource.class);
        final FeedArchiver archiver = getAdapter(archivalConfig.getFeedArchiver(), FeedArchiver.class);
        final int archivalInterval = 3600000;

        if (archiver != null) {
            //TODO: Implements archivalConfig.getIntervalSpec(); DOIT!
            archiver.setArchivalIntervalSpec(archivalInterval);
            feedArchivalService.registerArchiveTask(feedSource, archiver);
        }

        if (archiveSource != null) {
            final ArchiveAdapter archiveAdapter = new ArchiveAdapter(
                    feedTargetRegexBuilder.getArchivesResource(), feed, archiveSource, feedAdapter);

            // archive
            regexTargetResolver.setPattern(feedTargetRegexBuilder.toArchivesPattern(),
                    TargetType.TYPE_COLLECTION,
                    TargetRegexBuilder.getArchiveResolverFieldList());

            collections.add(archiveAdapter);
        }
    }
}
