package net.jps.atom.hopper.abdera;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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

    private final List<TargetAwareAbstractCollectionAdapter> namespaceCollectionAdapters;
    private final RegexTargetResolver regexTargetResolver;
    private final WorkspaceConfiguration myConfig;

    public WorkspaceHandler(WorkspaceConfiguration myConfig, RegexTargetResolver regexTargetResolver) {
        this.myConfig = myConfig;
        this.regexTargetResolver = regexTargetResolver;

        this.namespaceCollectionAdapters = new LinkedList<TargetAwareAbstractCollectionAdapter>();
    }

    public RegexTargetResolver getRegexTargetResolver() {
        return regexTargetResolver;
    }

    public void addCollectionAdapter(TargetAwareAbstractCollectionAdapter adapter) {
        namespaceCollectionAdapters.add(adapter);
    }

    public TargetAwareAbstractCollectionAdapter getAnsweringAdapter(RequestContext rc) {
        for (TargetAwareAbstractCollectionAdapter adapter : namespaceCollectionAdapters) {
            if (adapter.canHandleTarget(rc.getTargetPath())) {
                return adapter;
            }
        }

        return null;
    }

    @Override
    public Workspace asWorkspaceElement(RequestContext rc) {
        return null;
    }

    @Override
    public Collection<CollectionInfo> getCollections(RequestContext rc) {
        return (Collection) namespaceCollectionAdapters;
    }

    @Override
    public String getTitle(RequestContext rc) {
        return myConfig.getTitle();
    }
}
