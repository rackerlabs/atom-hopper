package net.jps.atom.hopper.abdera;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.WorkspaceInfo;

public class WorkspaceManager implements org.apache.abdera.protocol.server.WorkspaceManager {

    private final List<WorkspaceHandler> handlers;

    public WorkspaceManager() {
        handlers = new LinkedList<WorkspaceHandler>();
    }
    
    public void addWorkspaces(List<WorkspaceHandler> workspaces) {
        handlers.addAll(workspaces);
    }

    @Override
    public Collection<WorkspaceInfo> getWorkspaces(RequestContext request) {
        return (Collection) handlers;
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
