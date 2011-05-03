package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.adapter.TargetResolverField;
import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.GetFeedArchiveRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class GetFeedArchiveRequestImpl extends AbstractClientRequest implements GetFeedArchiveRequest {

    private String archiveMarker;

    public GetFeedArchiveRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);

        populateSelf();
    }

    private void populateSelf() {
        final RequestContext requestContext = getRequestContext();

        archiveMarker = requestContext.getTarget().getParameter(TargetResolverField.MARKER.name());
    }

    @Override
    public String getArchiveMarker() {
        return archiveMarker;
    }
}
