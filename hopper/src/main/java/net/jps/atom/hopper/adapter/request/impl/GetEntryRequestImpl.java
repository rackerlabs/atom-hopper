package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.adapter.TargetResolverField;
import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.GetEntryRequest;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 *
 */
public class GetEntryRequestImpl extends AbstractClientRequest implements GetEntryRequest {

    private String entryId;

    public GetEntryRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);

        populateSelf();
    }

    private void populateSelf() {
        final RequestContext requestContext = getRequestContext();

        entryId = requestContext.getTarget().getParameter(TargetResolverField.ENTRY.name());
    }

    @Override
    public Entry newEntry() {
        return getRequestContext().getAbdera().newEntry();
    }

    @Override
    public String getId() {
        return entryId;
    }
}
