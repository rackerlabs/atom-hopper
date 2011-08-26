package org.atomhopper.adapter.request.entry;

import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.abdera.TargetResolverField;
import org.atomhopper.adapter.request.feed.AbstractFeedRequest;

public class AbstractEntryRequest extends AbstractFeedRequest implements EntryRequest {

    private final String entryId;

    public AbstractEntryRequest(RequestContext abderaRequestContext) {
        super(abderaRequestContext);

        entryId = abderaRequestContext.getTarget().getParameter(TargetResolverField.ENTRY.name());
    }

    @Override
    public String getEntryId() {
        return entryId;
    }
}
