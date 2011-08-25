package org.atomhopper.adapter.request.adapter.impl;

import java.util.Calendar;
import org.atomhopper.adapter.request.adapter.GetFeedArchiveRequest;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.feed.AbstractFeedRequest;

public class GetFeedArchiveRequestImpl extends AbstractFeedRequest implements GetFeedArchiveRequest {

    public GetFeedArchiveRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public Calendar getRequestedArchiveDate() {
        return Calendar.getInstance();
    }
}
