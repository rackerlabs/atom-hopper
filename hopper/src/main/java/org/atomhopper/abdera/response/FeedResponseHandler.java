package org.atomhopper.abdera.response;

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.util.EntityTag;
import org.atomhopper.abdera.filter.AdapterResponseInterceptor;
import org.atomhopper.response.AdapterResponse;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FeedResponseHandler extends AbstractResponseHandler<Feed> {

    private static final String XML = "application/xml";

    public FeedResponseHandler(String[] allowedMethods, AdapterResponseInterceptor<Feed>... interceptors) {
        super(allowedMethods, interceptors);
    }

    @Override
    protected ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Date lastUpdated = adapterResponse.getBody() != null ? adapterResponse.getBody().getUpdated() : null;

        switch (adapterResponse.getResponseStatus()) {
            case OK:
            	ResponseContext responseContext;
            	if (entityTagMatches(rc.getIfNoneMatch(), adapterResponse.getEntityTag())) {
            		responseContext = ProviderHelper.notmodified(rc);
                }else{
                	responseContext = ProviderHelper.returnBase(adapterResponse.getBody(), adapterResponse.getResponseStatus().value(), lastUpdated);	
                }            	
                responseContext.setEntityTag(adapterResponse.getEntityTag());
                return responseContext;
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

    private boolean entityTagMatches(EntityTag[] ifNoneMatch, EntityTag entityTag) {
        if (ifNoneMatch == null || entityTag == null) {
            return false;
        }
        List<EntityTag> ifNoneMatchList = Arrays.asList(ifNoneMatch);
        return ifNoneMatchList.size() == 1 && ifNoneMatchList.get(0).getTag().equals(entityTag.getTag());
    }
}
