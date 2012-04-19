package org.atomhopper.config;

import org.apache.abdera.protocol.server.TargetType;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.abdera.FeedAdapter;
import org.atomhopper.abdera.TargetAwareAbstractCollectionAdapter;
import org.atomhopper.abdera.WorkspaceHandler;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.config.v1_0.AdapterDescriptor;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.config.v1_0.WorkspaceConfiguration;
import org.atomhopper.servlet.ApplicationContextAdapter;
import org.atomhopper.util.TargetRegexBuilder;
import org.atomhopper.util.context.AdapterGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * I eat configurations.
 *
 * TODO: Sanitize configured workspace and feed resource paths for regex insertion
 */
public class WorkspaceConfigProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceConfigProcessor.class);
    private final RegexTargetResolver regexTargetResolver;
    private final AdapterGetter adapterGetter;
    private final WorkspaceConfiguration config;
    private final TargetRegexBuilder targetRegexGenerator;

    //TODO: Consider builder pattern
    public WorkspaceConfigProcessor(WorkspaceConfiguration config, ApplicationContextAdapter contextAdapter, RegexTargetResolver regexTargetResolver, String contextPath) {
        this.config = config;
        this.adapterGetter = new AdapterGetter(contextAdapter);
        this.regexTargetResolver = regexTargetResolver;

        targetRegexGenerator = new TargetRegexBuilder();

        if (!StringUtils.isBlank(contextPath)) {
            targetRegexGenerator.setContextPath(contextPath);
        }
    }

    public List<WorkspaceHandler> toHandler() {
        final List<WorkspaceHandler> workspaces = new LinkedList<WorkspaceHandler>();

        for (TargetAwareAbstractCollectionAdapter collectionAdapter : assembleFeeds(config.getFeed())) {
            final WorkspaceHandler workspace = new WorkspaceHandler(config);
            workspace.addCollectionAdapter(new StringBuilder()
                    .append(config.getResource())
                    .append(collectionAdapter
                    .getTarget()).toString(), collectionAdapter);

            LOG.info("Loading Workspace: " + collectionAdapter.getTarget());
            workspaces.add(workspace);
        }

        return workspaces;
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
                LOG.error("Unable to find specified default adapter class: " + className, cnfe);

                throw new ConfigurationException("Unable to find specified default adapter class: " + className, cnfe);
            }
        }

        return resolvedReference;
    }
//
//    private Map<String, String> adapterParametersToMap(List<AdapterParameter> paramters) {
//        final Map<String, String> paramterMap = new HashMap<String, String>();
//    }

    public <T> T getAdapter(AdapterDescriptor descriptor, Class<T> expectedClass) {
//        final Map<String, String> parameters = descriptorParametersToMap(descriptor.getParameter());

        return descriptor != null
                ? getFromApplicationContext(descriptor.getReference(), descriptor.getClazz(), expectedClass)
                : null;
    }

    private List<TargetAwareAbstractCollectionAdapter> assembleFeedAdapters(TargetRegexBuilder workspaceTarget, List<FeedConfiguration> feeds) {
        final List<TargetAwareAbstractCollectionAdapter> collections = new LinkedList<TargetAwareAbstractCollectionAdapter>();

        for (FeedConfiguration feed : feeds) {
            final FeedSource feedSource = getAdapter(feed.getFeedSource(), FeedSource.class);

            final FeedPublisher feedPublisher = getAdapter(feed.getPublisher(), FeedPublisher.class);

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

            collections.add(feedAdapter);
        }

        return collections;
    }
}
