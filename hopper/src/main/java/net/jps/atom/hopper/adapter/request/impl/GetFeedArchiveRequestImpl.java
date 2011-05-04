package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.adapter.TargetResolverField;
import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.GetFeedArchiveRequest;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class GetFeedArchiveRequestImpl extends AbstractClientRequest implements GetFeedArchiveRequest {

    private String archiveId;

    public GetFeedArchiveRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);

        populateSelf();
    }

    private void populateSelf() {
        final RequestContext requestContext = getRequestContext();

        archiveId = requestContext.getTarget().getParameter(TargetResolverField.ARCHIVE.name());
    }

    @Override
    public String getArchiveId() {
        return archiveId;
    }

    @Override
    public Feed newFeed() {
        return getRequestContext().getAbdera().newFeed();
    }
}
