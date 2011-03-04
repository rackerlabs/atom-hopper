package net.jps.atom.hopper.config;

import net.jps.atom.hopper.abdera.SenseFeedAdapter;
import net.jps.atom.hopper.config.v1_0.WorkspaceConfig;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.model.Workspace;
import org.apache.abdera.protocol.server.CollectionInfo;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.impl.RegexTargetResolver;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;

public class WorkspaceHandler implements WorkspaceInfo {

    private final List<SenseFeedAdapter> namespaceCollectionAdapters;
    private final RegexTargetResolver regexTargetResolver;
    private final TemplateTargetBuilder templateTargetBuilder;
    private final WorkspaceConfig myConfig;

    public WorkspaceHandler(WorkspaceConfig myConfig, RegexTargetResolver regexTargetResolver, TemplateTargetBuilder templateTargetBuilder) {
        this.myConfig = myConfig;
        this.regexTargetResolver = regexTargetResolver;
        this.templateTargetBuilder = templateTargetBuilder;

        this.namespaceCollectionAdapters = new LinkedList<SenseFeedAdapter>();
    }

    public RegexTargetResolver getRegexTargetResolver() {
        return regexTargetResolver;
    }

    public TemplateTargetBuilder getTemplateTargetBuilder() {
        return templateTargetBuilder;
    }

    public void addCollectionAdapter(SenseFeedAdapter adapter) {
        namespaceCollectionAdapters.add(adapter);
    }

    public SenseFeedAdapter getAnsweringAdapter(RequestContext rc) {
        for (SenseFeedAdapter adapter : namespaceCollectionAdapters) {
            if (adapter.handles(rc.getTargetPath())) {
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
