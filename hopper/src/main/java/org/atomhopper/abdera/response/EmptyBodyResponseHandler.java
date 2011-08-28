package org.atomhopper.abdera.response;

import org.atomhopper.abdera.filter.AdapterResponseInterceptor;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

public class EmptyBodyResponseHandler extends AbstractResponseHandler<EmptyBody> {

    public EmptyBodyResponseHandler(String[] allowedMethods, AdapterResponseInterceptor<EmptyBody>... interceptors) {
        super(allowedMethods, interceptors);
    }

    @Override
    protected ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<EmptyBody> adapterResponse) {
        switch (adapterResponse.getResponseStatus()) {
            case NOT_FOUND:
                return ProviderHelper.notfound(rc, adapterResponse.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, adapterResponse.getMessage(), new InternalServerException());

            case METHOD_NOT_ALLOWED:
                return ProviderHelper.notallowed(rc, adapterResponse.getMessage(), getAllowedHttpMethods());
                
            default:
                return ProviderHelper.nocontent();
        }
    }
}
