package org.atomhopper.adapter.request.adapter.impl;

import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.entry.AbstractEntryRequest;

/**
 *
 * 
 */
public class DeleteEntryRequestImpl extends AbstractEntryRequest implements DeleteEntryRequest {

    public DeleteEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }
}
