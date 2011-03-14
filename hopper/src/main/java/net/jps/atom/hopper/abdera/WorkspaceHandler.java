package net.jps.atom.hopper.abdera;

import com.rackspace.cloud.commons.util.StringUtilities;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.jps.atom.hopper.adapter.TargetResolverField;
import net.jps.atom.hopper.config.v1_0.WorkspaceConfiguration;
import org.apache.abdera.model.Workspace;
import org.apache.abdera.protocol.server.CollectionInfo;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;

/**
 * TODO: Re-add support for TargetBuilder for URL construction
 *
 *
 */

public class WorkspaceHandler implements WorkspaceInfo {

    private final Map<String, TargetAwareAbstractCollectionAdapter> collectionAdapterMap;
    private final RegexTargetResolver regexTargetResolver;
    private final WorkspaceConfiguration myConfig;

    public WorkspaceHandler(WorkspaceConfiguration myConfig, RegexTargetResolver regexTargetResolver) {
        this.myConfig = myConfig;
        this.regexTargetResolver = regexTargetResolver;

        this.collectionAdapterMap = new HashMap<String, TargetAwareAbstractCollectionAdapter>();
    }

    public RegexTargetResolver getRegexTargetResolver() {
        return regexTargetResolver;
    }

    public void addCollectionAdapter(String collectionId, TargetAwareAbstractCollectionAdapter adapter) {
        collectionAdapterMap.put(collectionId, adapter);
    }

    public TargetAwareAbstractCollectionAdapter getAnsweringAdapter(RequestContext rc) {
        final String feedSpec = rc.getTarget().getParameter(TargetResolverField.FEED.toString());

        return !StringUtilities.isBlank(feedSpec) ? collectionAdapterMap.get(feedSpec) : null;
    }

    @Override
    public Workspace asWorkspaceElement(RequestContext rc) {
        //TODO: Implement this D:
        return null;
    }

    @Override
    public Collection<CollectionInfo> getCollections(RequestContext rc) {
        return (Collection) Collections.unmodifiableCollection(collectionAdapterMap.values());
    }

    @Override
    public String getTitle(RequestContext rc) {
        return myConfig.getTitle();
    }
}
