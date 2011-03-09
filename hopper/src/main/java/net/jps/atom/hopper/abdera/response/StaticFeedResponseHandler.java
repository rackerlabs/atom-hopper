package net.jps.atom.hopper.abdera.response;

import java.util.Date;
import net.jps.atom.hopper.abdera.filter.FeedPagingProcessor;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

/**
 *
 * 
 */
public class StaticFeedResponseHandler extends AbstractResponseHandler<Feed> {
    
    public StaticFeedResponseHandler() {
        super(new FeedPagingProcessor());
    }
    
    @Override
    public ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Date lastUpdated = adapterResponse.getBody() != null ? adapterResponse.getBody().getUpdated() : null;
        
        switch (adapterResponse.getResponseStatus()) {
            case OK:
                processResponse(rc, adapterResponse);
                return ProviderHelper.returnBase(adapterResponse.getBody(), adapterResponse.getResponseStatus().intValue(), lastUpdated);
            
            case NOT_FOUND:
                return ProviderHelper.notfound(rc, adapterResponse.getMessage());
            
            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, adapterResponse.getMessage(), new InternalServerException());
            
            default:
                return ProviderHelper.notfound(rc);
        }
    }
}
