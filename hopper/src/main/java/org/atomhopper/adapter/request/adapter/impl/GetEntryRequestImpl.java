package org.atomhopper.adapter.request.adapter.impl;

import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.entry.AbstractEntryRequest;

public class GetEntryRequestImpl extends AbstractEntryRequest implements GetEntryRequest {

    public GetEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }
    
    @Override
    public Entry newEntry() {
        return getAbdera().newEntry();
    }

}
