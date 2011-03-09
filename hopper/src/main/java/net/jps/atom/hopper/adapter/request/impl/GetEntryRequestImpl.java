/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.abdera.TargetResolverField;
import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.GetEntryRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class GetEntryRequestImpl extends AbstractClientRequest implements GetEntryRequest {

    private String entryId;
    
    public GetEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
        
        populateSelf();
    }

    private void populateSelf() {
        final RequestContext requestContext = getRequestContext();
        
        entryId = requestContext.getParameter(TargetResolverField.ENTRY.name());
    }
    
    @Override
    public String getId() {
        return entryId;
    }
}
