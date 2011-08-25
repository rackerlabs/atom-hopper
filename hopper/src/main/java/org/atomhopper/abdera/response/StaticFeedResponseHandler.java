package org.atomhopper.abdera.response;

import org.atomhopper.abdera.filter.FeedConfigurationResponseProcessor;
import org.atomhopper.abdera.filter.FeedPagingProcessor;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

import java.util.Date;

/**
 *
 * 
 */
public class StaticFeedResponseHandler extends AbstractResponseHandler<Feed> {
    
    public StaticFeedResponseHandler(FeedConfiguration feedConfiguration) {
        super(new FeedPagingProcessor(), new FeedConfigurationResponseProcessor(feedConfiguration));
    }
    
    @Override
    public ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Date lastUpdated = adapterResponse.getBody() != null ? adapterResponse.getBody().getUpdated() : null;
        
        switch (adapterResponse.getResponseStatus()) {
            case OK:
                processResponse(rc, adapterResponse);
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
