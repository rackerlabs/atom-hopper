package net.jps.atom.hopper.abdera.response;

import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

/**
 *
 * 
 */
public class StaticEmptyBodyResponseHandler extends AbstractResponseHandler<EmptyBody> {

    public StaticEmptyBodyResponseHandler() {
        super();
    }

    @Override
    public ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<EmptyBody> adapterResponse) {
        switch (adapterResponse.getResponseStatus()) {
            case NOT_FOUND:
                return ProviderHelper.notfound(rc, adapterResponse.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, adapterResponse.getMessage(), new InternalServerException());

            default:
                return ProviderHelper.nocontent();
        }
    }
}
