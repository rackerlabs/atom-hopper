package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.DeleteEntryRequest;
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
