package net.jps.atom.hopper.abdera;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;

public class WorkspaceManager implements org.apache.abdera.protocol.server.WorkspaceManager {

    private final TemplateTargetBuilder targetBuilder;
    private final List<WorkspaceHandler> handlers;

    public WorkspaceManager(TemplateTargetBuilder targetBuilder) {
        handlers = new LinkedList<WorkspaceHandler>();
        this.targetBuilder = targetBuilder;
    }

    public void addWorkspace(WorkspaceHandler workspace) {
        handlers.add(workspace);
    }

    @Override
    public Collection<WorkspaceInfo> getWorkspaces(RequestContext request) {
        return (Collection) handlers;
    }

    public String urlFor(RequestContext request, Object key, Object param) {
        return targetBuilder.urlFor(request, key, param);
    }

    public Target resolveTarget(RequestContext rc) {
        for (WorkspaceHandler handler : handlers) {
            final Target target = handler.getRegexTargetResolver().resolve(rc);

            if (target != null) {
                return target;
            }
        }

        return null;
    }

    @Override
    public CollectionAdapter getCollectionAdapter(RequestContext request) {
        for (WorkspaceHandler workspace : handlers) {
            final CollectionAdapter adapter = workspace.getAnsweringAdapter(request);

            if (adapter != null) {
                return adapter;
            }
        }

        return null;
    }
}
