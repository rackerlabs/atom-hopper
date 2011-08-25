package org.atomhopper.adapter.request.impl;

import org.atomhopper.adapter.request.AbstractClientRequest;
import org.atomhopper.adapter.request.DeleteEntryRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class DeleteEntryRequestImpl extends AbstractClientRequest implements DeleteEntryRequest {

    public DeleteEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }
    
    @Override
    public String getId() {
        return "";
    }
}
