package org.atomhopper.adapter.request.feed;

import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.abdera.TargetResolverField;
import org.atomhopper.adapter.request.AbstractClientRequest;

public abstract class AbstractFeedRequest extends AbstractClientRequest implements FeedRequest {

    public AbstractFeedRequest(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public String getFeedName() {
        return getTargetParameter(TargetResolverField.FEED.name());
    }
}
