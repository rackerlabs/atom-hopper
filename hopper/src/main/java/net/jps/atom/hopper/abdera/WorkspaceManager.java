package net.jps.atom.hopper.abdera;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.jps.atom.hopper.adapter.TemplateTarget;
import org.apache.abdera.protocol.server.CollectionAdapter;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.protocol.server.WorkspaceInfo;
import org.apache.abdera.protocol.server.impl.TemplateTargetBuilder;

public class WorkspaceManager implements org.apache.abdera.protocol.server.WorkspaceManager {

    private final TemplateTargetBuilder targetBuilder;
    private final List<WorkspaceHandler> handlers;

    public WorkspaceManager() {
        handlers = new LinkedList<WorkspaceHandler>();

        targetBuilder = new TemplateTargetBuilder();
        targetBuilder.setTemplate(TemplateTarget.WORKSPACE, "{target_base}/{workspace}/");
        targetBuilder.setTemplate(TemplateTarget.FEED, "{target_base}/{workspace}/{feed}{-prefix|/|entry}/{-opt|?|categories,marker,limit}{-join|&|categories,marker,limit}");
        targetBuilder.setTemplate(TemplateTarget.FEED_CATEGORIES, "{target_base}/{workspace}/{feed}/categories/");
        targetBuilder.setTemplate(TemplateTarget.FEED_ARCHIVES, "{target_base}/{workspace}/{feed}/archives/{-prefix|/|year}{-prefix|/|month}{-prefix|/|day}{-prefix|/|time}");
    }

    public void addWorkspace(WorkspaceHandler workspace) {
        handlers.add(workspace);
    }

    @Override
    public Collection<WorkspaceInfo> getWorkspaces(RequestContext request) {
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
        for (WorkspaceHandler workspace : handlers) {
            final CollectionAdapter adapter = workspace.getAnsweringAdapter(request);

            if (adapter != null) {
                return adapter;
            }
        }

        return null;
    }
}
