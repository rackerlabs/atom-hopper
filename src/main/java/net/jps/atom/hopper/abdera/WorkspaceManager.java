package net.jps.atom.hopper.abdera;

import net.jps.atom.hopper.config.WorkspaceHandler;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.WorkspaceInfo;

public class WorkspaceManager implements org.apache.abdera.protocol.server.WorkspaceManager {

    private final List<WorkspaceHandler> handlers;

    public WorkspaceManager() {
        handlers = new LinkedList<WorkspaceHandler>();
    }

    public void addWorkspace(WorkspaceHandler workspace) {
        handlers.add(workspace);
    }

    @Override
    public Collection<WorkspaceInfo> getWorkspaces(RequestContext request) {
        //TODO: Scope this to the root of the request
        return (Collection) handlers;
    }

    //TODO: Reimplement and integrate with the regex target builder
    public String urlFor(RequestContext request, Object key, Object param) {
//        for (WorkspaceHandler handler : handlers) {
//            final String url = handler.getTemplateTargetBuilder().urlFor(request, key, param);
//
//            if (url != null) {
//                return url;
//            }
//        }

        return null;
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
        for (WorkspaceHandler currentAdapter : handlers) {
            final CollectionAdapter adapter = currentAdapter.getAnsweringAdapter(request);

            if (adapter != null) {
                return adapter;
            }
        }

        return null;
    }
}
