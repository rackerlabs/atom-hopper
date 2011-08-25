package org.atomhopper.adapter.request.impl;

import org.atomhopper.adapter.request.AbstractClientRequest;
import org.atomhopper.adapter.request.PutEntryRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public class PutEntryRequestImpl extends AbstractClientRequest implements PutEntryRequest {

    public PutEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public String getId() {
        return "";
    }
}
