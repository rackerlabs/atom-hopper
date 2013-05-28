package org.atomhopper.abdera.response;

import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.atomhopper.abdera.filter.AdapterResponseInterceptor;
import org.atomhopper.response.AdapterResponse;

import java.util.Date;

public class EntryResponseHandler extends AbstractResponseHandler<Entry> {

    private static final String XML = "application/xml";

    public EntryResponseHandler(String[] allowedMethods, AdapterResponseInterceptor<Entry>... interceptors) {
        super(allowedMethods, interceptors);
    }

    @Override
    protected ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<Entry> adapterResponse) {
        final Date lastUpdated = adapterResponse.getBody() != null ? adapterResponse.getBody().getUpdated() : null;

        switch (adapterResponse.getResponseStatus()) {
            case OK:
            case CREATED:
                return ProviderHelper.returnBase(adapterResponse.getBody(), adapterResponse.getResponseStatus().value(), lastUpdated);

            case NOT_FOUND:
                return ProviderHelper.notfound(rc, adapterResponse.getMessage()).setContentType(XML);

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, adapterResponse.getMessage(), new InternalServerException()).setContentType(XML);

            case METHOD_NOT_ALLOWED:
                return ProviderHelper.notallowed(rc, adapterResponse.getMessage(), getAllowedHttpMethods()).setContentType(XML);

            case BAD_REQUEST:
                return ProviderHelper.badrequest(rc, adapterResponse.getMessage()).setContentType(XML);

            case CONFLICT:
                return ProviderHelper.conflict(rc, adapterResponse.getMessage()).setContentType(XML);
                
            default:
                return ProviderHelper.notfound(rc).setContentType(XML);
        }
    }
}
