package org.atomhopper.adapter.request.adapter.impl;

import org.atomhopper.adapter.request.feed.AbstractFeedRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class PostEntryRequestImpl extends AbstractFeedRequest implements PostEntryRequest {

    public PostEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public Entry getEntry() {
        try {
            return getRequestContext().<Entry>getDocument().getRoot();
        } catch (Exception ex) {
            throw new RequestParsingException("Failed to read in ATOM Entry data. Reason: " + ex.getMessage(), ex);
        }
    }
}
