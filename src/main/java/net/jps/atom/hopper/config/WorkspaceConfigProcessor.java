package net.jps.atom.hopper.config;

import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import net.jps.atom.hopper.abdera.FeedAdapter;
import net.jps.atom.hopper.abdera.TargetResolverField;
import net.jps.atom.hopper.archive.FeedArchivalService;
import net.jps.atom.hopper.adapter.archive.FeedArchiveAdapter;

import java.util.LinkedList;
import java.util.List;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.adapter.FeedAdapterTools;
import net.jps.atom.hopper.config.v1_0.ArchiveConfiguration;
import net.jps.atom.hopper.config.v1_0.FeedConfiguration;
import net.jps.atom.hopper.config.v1_0.WorkspaceConfiguration;
import net.jps.atom.hopper.util.TargetRegexBuilder;
import net.jps.atom.hopper.util.context.AdapterGetter;

import org.apache.abdera.Abdera;
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
    private final Abdera abderaReference;
    private FeedArchiveAdapter defaultArchiver;
    private FeedSourceAdapter defaultFeedSource;

    public WorkspaceConfigProcessor(WorkspaceConfiguration workspace, ApplicationContextAdapter contextAdapter, Abdera abderaReference, FeedArchivalService feedArchivalService) {
        this.config = workspace;
        this.adapterGetter = new AdapterGetter(contextAdapter);
        this.feedArchivalService = feedArchivalService;
        this.abderaReference = abderaReference;
    }

    public WorkspaceHandler toHandler() {
        final List<FeedAdapter> namespaceCollectionAdapters = new LinkedList<FeedAdapter>();
        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();

        final WorkspaceHandler workspace = new WorkspaceHandler(config, regexTargetResolver);

        for (FeedAdapter collectionAdapter : assembleServices(config.getFeed(), namespaceCollectionAdapters, regexTargetResolver)) {
            workspace.addCollectionAdapter(collectionAdapter);
        }

        return workspace;
    }

    private void setDefaults(WorkspaceConfiguration workspaceConfig) {
        if (!StringUtilities.isBlank(workspaceConfig.getDefaultAdapterRef()) || !StringUtilities.isBlank(workspaceConfig.getDefaultAdapterClass())) {
            defaultFeedSource = getFeedSource(workspaceConfig.getDefaultAdapterRef(), workspaceConfig.getDefaultAdapterClass());
        }

        if (workspaceConfig.getArchive() != null) {
            final ArchiveConfiguration archiveDefault = workspaceConfig.getArchive();

            defaultArchiver = getArchiveAdapter(archiveDefault.getArchiverRef(), archiveDefault.getArchiverClass());
        }
    }

    //TODO: compose the two methods below to avoid copy-pasta
    private FeedSourceAdapter getFeedSource(String adapterRef, String adapterClass) throws ConfigurationException {
        if (!StringUtilities.isBlank(adapterRef)) {
            return adapterGetter.getFeedSource(adapterRef);
        } else if (!StringUtilities.isBlank(adapterClass)) {
            try {
                adapterGetter.getFeedSource(Class.forName(adapterClass));
            } catch (ClassNotFoundException cnfe) {
                throw LOG.newException("Unable to find specified default adapter class: " + adapterClass, cnfe, ConfigurationException.class);
            }

            return adapterGetter.getFeedSource(config.getDefaultAdapterRef());
        }

        return defaultFeedSource;
    }

    private FeedArchiveAdapter getArchiveAdapter(String adapterRef, String adapterClass) throws ConfigurationException {
        if (!StringUtilities.isBlank(adapterRef)) {
            return adapterGetter.getFeedArchive(adapterRef);
        } else if (!StringUtilities.isBlank(adapterClass)) {
            try {
                adapterGetter.getFeedSource(Class.forName(adapterClass));
            } catch (ClassNotFoundException cnfe) {
                throw LOG.newException("Unable to find specified default adapter class: " + adapterClass, cnfe, ConfigurationException.class);
            }

            return adapterGetter.getFeedArchive(config.getDefaultAdapterRef());
        }

        return defaultArchiver;
    }

    private List<FeedAdapter> assembleServices(List<FeedConfiguration> feedServices, List<FeedAdapter> namespaceCollectionAdapters, RegexTargetResolver regexTargetResolver) {
        final List<FeedAdapter> collections = new LinkedList<FeedAdapter>();

        final String workspaceName = StringUtilities.trim(config.getResourceBase(), "/");
        final TargetRegexBuilder workspaceTarget = new TargetRegexBuilder();

        workspaceTarget.setWorkspace(workspaceName);

        // service
        regexTargetResolver.setPattern(workspaceTarget.toWorkspacePattern(),
                TargetType.TYPE_SERVICE,
                TargetResolverField.WORKSPACE.name());

        // categories
        regexTargetResolver.setPattern(workspaceTarget.toCategoryPattern(),
                TargetType.TYPE_CATEGORIES,
                TargetResolverField.WORKSPACE.name());

        for (FeedAdapter adapter : assembleFeedAdapters(workspaceTarget, feedServices, regexTargetResolver)) {
            collections.add(adapter);
            namespaceCollectionAdapters.add(adapter);
        }

        return collections;
    }

    private List<FeedAdapter> assembleFeedAdapters(TargetRegexBuilder workspaceTarget, List<FeedConfiguration> feeds, RegexTargetResolver regexTargetResolver) {
        final List<FeedAdapter> collections = new LinkedList<FeedAdapter>();

        for (FeedConfiguration feed : feeds) {
            final FeedSourceAdapter feedSource = getFeedSource(feed.getAdapterRef(), feed.getAdapterClass());

            feedSource.setAdapterTools(new FeedAdapterTools(abderaReference));

            final FeedAdapter adapter = new FeedAdapter(feed, feedSource);
            final String feedResource = StringUtilities.trim(feed.getResource(), "/");

            final TargetRegexBuilder feedTargetRegexBuilder = new TargetRegexBuilder(workspaceTarget);
            feedTargetRegexBuilder.setFeed(feedResource);

            final String feedRegex = feedTargetRegexBuilder.toFeedPattern();
            final String entryRegex = feedTargetRegexBuilder.toEntryPattern();

            adapter.addTargetRegex(feedRegex);
            adapter.addTargetRegex(entryRegex);

            // feed
            regexTargetResolver.setPattern(feedRegex,
                    TargetType.TYPE_COLLECTION,
                    TargetResolverField.WORKSPACE.name(),
                    TargetResolverField.FEED.name());

            // entry
            regexTargetResolver.setPattern(entryRegex,
                    TargetType.TYPE_ENTRY,
                    TargetResolverField.WORKSPACE.name(),
                    TargetResolverField.FEED.name(),
                    TargetResolverField.ENTRY.name());


            collections.add(adapter);
            addArchiver(feed, feedSource);
        }

        return collections;
    }

    //TODO: Implement the default archiver if archival isn't explicitly set
    private void addArchiver(FeedConfiguration feed, FeedSourceAdapter feedSource) {
        final ArchiveConfiguration archivalElement = feed.getArchive();

        if (archivalElement != null) {
            final FeedArchiveAdapter archiver = getArchiveAdapter(archivalElement.getArchiverRef(), archivalElement.getArchiverClass());

            //TODO: Protect this with a try statement that captures internal exceptions
            archiver.setAdapterTools(new FeedAdapterTools(abderaReference));

            try {
                archiver.setArchivalInterval(archivalElement.getArchivalInterval());
            } catch (UnsupportedOperationException uoe) {
                LOG.warn("Archiver class: "
                        + archiver.getClass().getName()
                        + " does not support time interval setting.", uoe);
            }

            feedArchivalService.registerArchiveTask(feedSource, archiver);
        }
    }
}
