package net.jps.atom.hopper.config;

import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.reflection.ReflectionTools;
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

import org.apache.abdera.Abdera;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;

/**
 * TODO: Decompose logic
 * TODO: Sanitize configured workspace and feed resource paths for regex insertion
 * 
 * @author zinic
 */
public class WorkspaceConfigProcessor {

    private static final Logger LOG = new RCLogger(WorkspaceConfigProcessor.class);
    public static final long HOUR_IN_MILLISECONDS = 3600000;
    
    private final FeedArchivalService feedArchivalService;
    private final ApplicationContextAdapter contextAdapter;
    private final WorkspaceConfiguration config;
    private final Abdera abderaReference;
    
    private FeedArchiveAdapter defaultArchiver;
    private FeedSourceAdapter defaultNamespaceAdapter;

    public WorkspaceConfigProcessor(WorkspaceConfiguration workspace, ApplicationContextAdapter contextAdapter, Abdera abderaReference, FeedArchivalService feedArchivalService) {
        this.config = workspace;
        this.contextAdapter = contextAdapter;
        this.feedArchivalService = feedArchivalService;
        this.abderaReference = abderaReference;
    }

    public WorkspaceHandler toHandler() {
        final List<FeedAdapter> namespaceCollectionAdapters = new LinkedList<FeedAdapter>();
        final RegexTargetResolver regexTargetResolver = new RegexTargetResolver();

        final WorkspaceHandler workspace = new WorkspaceHandler(config, regexTargetResolver);

        defaultNamespaceAdapter = getFromAppContext(config.getDefaultAdapterRef(), config.getDefaultAdapterClass(), FeedSourceAdapter.class);
        defaultArchiver = getWorkspaceFeedArchiver(config);

        for (FeedAdapter collectionAdapter : assembleServices(config.getFeed(), namespaceCollectionAdapters, regexTargetResolver)) {
            workspace.addCollectionAdapter(collectionAdapter);
        }

        return workspace;
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

        for (FeedAdapter adapter : assembleFeedAdapters(workspaceTarget, feedServices, workspaceName, regexTargetResolver)) {
            collections.add(adapter);
            namespaceCollectionAdapters.add(adapter);
        }

        return collections;
    }

    private List<FeedAdapter> assembleFeedAdapters(TargetRegexBuilder workspaceTarget, List<FeedConfiguration> feeds, String namespace, RegexTargetResolver regexTargetResolver) {
        final List<FeedAdapter> collections = new LinkedList<FeedAdapter>();

        for (FeedConfiguration feed : feeds) {
            final FeedSourceAdapter feedSource = getFeedSourceAdapter(feed);

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

    private void addArchiver(FeedConfiguration feed, FeedSourceAdapter feedSource) {
        final ArchiveConfiguration archivalElement = feed.getArchive();

        if (archivalElement != null) {
            final FeedArchiveAdapter archiver = getFeedArchiver(archivalElement);

            //TODO: Protect this with a try statement that captures internal exceptions
            archiver.init(new FeedAdapterTools(abderaReference), feedSource);

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

    private FeedArchiveAdapter getFeedArchiver(ArchiveConfiguration archiveMarker) {
        if (!StringUtilities.isBlank(archiveMarker.getArchiverClass()) || !StringUtilities.isBlank(archiveMarker.getArchiverRef())) {
            return getFromAppContext(archiveMarker.getArchiverRef(), archiveMarker.getArchiverClass(), FeedArchiveAdapter.class);
        }

        return defaultArchiver;
    }

    private FeedArchiveAdapter getWorkspaceFeedArchiver(WorkspaceConfiguration workspace) {
        final ArchiveConfiguration archiveConfig = workspace.getArchive();

        if (archiveConfig != null) {
            if (!StringUtilities.isBlank(archiveConfig.getArchiverClass())) {
                return getFromAppContext(archiveConfig.getArchiverRef(), archiveConfig.getArchiverClass(), FeedArchiveAdapter.class);
            } else if (StringUtilities.isBlank(archiveConfig.getArchiverRef())) {
//                return new FileSystemFeedArchiver(); //TODO: Add dir configuration for this
            }
        }

        return null;
    }

    private FeedSourceAdapter getFeedSourceAdapter(FeedConfiguration feed) {
        final FeedSourceAdapter feedSpecificAdapter = getFromAppContext(feed.getAdapterRef(), feed.getAdapterClass(), FeedSourceAdapter.class);

        if (feedSpecificAdapter == null && defaultNamespaceAdapter == null) {
            throw new ConfigurationException("Failed to find or build an appropriate FeedSourceAdapter for feed: " + feed.getTitle());
        }

        return feedSpecificAdapter != null ? feedSpecificAdapter : defaultNamespaceAdapter;
    }

    /**
     * 
     * @param <T>
     * @param referenceName
     * @param absoluteClassName
     * @param parentClassDefinition
     * @return 
     */
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
                throw LOG.newException("Class: " + absoluteClassName + " can not be found. Please check your configuration.", cnfe, ConfigurationException.class);
            } catch (Exception ex) {
                throw LOG.newException("Error occured while trying to source class information. Please check your configuration. Reason: " + ex.getMessage(), ConfigurationException.class);
            }
        }


        return objectFromContext;
    }
}
