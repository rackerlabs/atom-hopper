package org.atomhopper.adapter.request.impl;

import org.atomhopper.adapter.request.AbstractClientRequest;
import org.atomhopper.adapter.request.PostEntryRequest;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class PostEntryRequestImpl extends AbstractClientRequest implements PostEntryRequest {

    private final Entry entryRef;

    public PostEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);

        try {
            entryRef = abderaRequestContext.<Entry>getDocument().getRoot();
        } catch (Exception ex) {
            throw new RequestParsingException("Failed to read in ATOM Entry data. Reason: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Entry getEntry() {
        return entryRef;
    }
}
