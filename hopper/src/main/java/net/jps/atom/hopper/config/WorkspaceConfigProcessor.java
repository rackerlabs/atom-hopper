package net.jps.atom.hopper.config;

import net.jps.atom.hopper.abdera.WorkspaceHandler;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import net.jps.atom.hopper.abdera.FeedAdapter;
import net.jps.atom.hopper.adapter.TargetResolverField;
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
 *
 */
public class WorkspaceConfigProcessor {

    private static final Logger LOG = new RCLogger(WorkspaceConfigProcessor.class);
    public static final long HOUR_IN_MILLISECONDS = 3600000;
    private final FeedArchivalService feedArchivalService;
    private final AdapterGetter adapterGetter;
    private final WorkspaceConfiguration config;
    private final TargetRegexBuilder targetRegexGenerator;

    //TODO: Consider builder pattern
    public WorkspaceConfigProcessor(WorkspaceConfiguration config, ApplicationContextAdapter contextAdapter, FeedArchivalService feedArchivalService, String contextPath) {
        this.config = config;
        this.adapterGetter = new AdapterGetter(contextAdapter);
        this.feedArchivalService = feedArchivalService;

        targetRegexGenerator = new TargetRegexBuilder();

        if (!StringUtilities.isBlank(contextPath)) {
            targetRegexGenerator.setContextPath(contextPath);
        }
    }

    public WorkspaceHandler toHandler() {
//        final TemplateTargetBuilder ttb = new TemplateTargetBuilder();


        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();
        final WorkspaceHandler workspace = new WorkspaceHandler(config, regexTargetResolver);

        for (TargetAwareAbstractCollectionAdapter collectionAdapter : assembleFeeds(config.getFeed(), regexTargetResolver)) {
            workspace.addCollectionAdapter(collectionAdapter.getTarget(), collectionAdapter);
        }

        return workspace;
    }

    private List<TargetAwareAbstractCollectionAdapter> assembleFeeds(List<FeedConfiguration> feedServices, RegexTargetResolver regexTargetResolver) {
        final List<TargetAwareAbstractCollectionAdapter> collections = new LinkedList<TargetAwareAbstractCollectionAdapter>();

        final String workspaceName = StringUtilities.trim(config.getResource(), "/");

        targetRegexGenerator.setWorkspace(workspaceName);

        // service
        regexTargetResolver.setPattern(targetRegexGenerator.toWorkspacePattern(),
                TargetType.TYPE_SERVICE,
                TargetRegexBuilder.getWorkspaceResolverFieldList());

        // categories
        regexTargetResolver.setPattern(targetRegexGenerator.toCategoryPattern(),
                TargetType.TYPE_CATEGORIES,
                TargetResolverField.WORKSPACE.name());

        for (TargetAwareAbstractCollectionAdapter adapter : assembleFeedAdapters(targetRegexGenerator, feedServices, regexTargetResolver)) {
            collections.add(adapter);
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

    public <T> T getAdapter(AdapterDescriptor descriptor, Class<T> expectedClass) {
        final T adapter = descriptor != null
                ? getFromApplicationContext(descriptor.getReference(), descriptor.getClazz(), expectedClass)
                : null;

        return adapter;
    }

    private List<TargetAwareAbstractCollectionAdapter> assembleFeedAdapters(TargetRegexBuilder workspaceTarget, List<FeedConfiguration> feeds, RegexTargetResolver regexTargetResolver) {
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

        if (archiver != null) {
            //TODO: Implements archivalConfig.getIntervalSpec();
            archiver.setArchivalIntervalSpec(3600000);
            feedArchivalService.registerArchiveTask(feedSource, archiver);
        }

        if (archiveSource != null) {
            final ArchiveAdapter archiveAdapter = new ArchiveAdapter(
                    feedTargetRegexBuilder.getArchivesResource(), archiveSource, feedAdapter);

            // archive
            regexTargetResolver.setPattern(feedTargetRegexBuilder.toArchivePattern(),
                    TargetType.TYPE_COLLECTION,
                    TargetRegexBuilder.getArchiveResolverFieldList());

            collections.add(archiveAdapter);
        }
    }
}
