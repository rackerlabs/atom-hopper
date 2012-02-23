package org.atomhopper.adapter.request.adapter.impl;

import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.adapter.GetFeedArchiveRequest;
import org.atomhopper.adapter.request.feed.AbstractFeedRequest;

import java.util.Calendar;

public class GetFeedArchiveRequestImpl extends AbstractFeedRequest implements GetFeedArchiveRequest {

    public GetFeedArchiveRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public Calendar getRequestedArchiveDate() {
        return Calendar.getInstance();
    }
}
