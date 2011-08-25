package org.atomhopper.abdera.response;

import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

import java.util.Date;

/**
 *
 * 
 */
public class StaticEntryResponseHandler extends AbstractResponseHandler<Entry> {

    public StaticEntryResponseHandler() {
        super();
    }

    @Override
    public ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<Entry> adapterResponse) {
        final Date lastUpdated = adapterResponse.getBody() != null ? adapterResponse.getBody().getUpdated() : null;

        switch (adapterResponse.getResponseStatus()) {
            case OK:
            case CREATED:
                return ProviderHelper.returnBase(adapterResponse.getBody(), adapterResponse.getResponseStatus().value(), lastUpdated);

            case NOT_FOUND:
                return ProviderHelper.notfound(rc, adapterResponse.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, adapterResponse.getMessage(), new InternalServerException());

            default:
                return ProviderHelper.notfound(rc);
        }
    }
}
