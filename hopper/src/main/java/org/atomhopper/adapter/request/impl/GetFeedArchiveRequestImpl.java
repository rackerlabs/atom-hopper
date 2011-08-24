package org.atomhopper.adapter.request.impl;

import java.util.Calendar;
import org.atomhopper.adapter.request.AbstractClientRequest;
import org.atomhopper.adapter.request.GetFeedArchiveRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class GetFeedArchiveRequestImpl extends AbstractClientRequest implements GetFeedArchiveRequest {

    public GetFeedArchiveRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public Calendar getRequestedArchiveDate() {
        return Calendar.getInstance();
    }
}
