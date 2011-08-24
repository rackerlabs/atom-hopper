package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.PutEntryRequest;
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
