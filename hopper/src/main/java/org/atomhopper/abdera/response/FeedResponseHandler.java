package org.atomhopper.abdera.response;

import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.util.EntityTag;
import org.atomhopper.abdera.filter.AdapterResponseInterceptor;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.response.AdapterResponse;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FeedResponseHandler extends AbstractResponseHandler<Feed> {

    private static final String XML = "application/xml";
    private static final String LINK_HEADER_NAME = "Link";
    private static final String LT = "<";
    private static final String GT = ">";
    private static final String LINK_SEPARATOR = ", ";

    public FeedResponseHandler(String[] allowedMethods, AdapterResponseInterceptor<Feed>... interceptors) {
        super(allowedMethods, interceptors);
    }

    public FeedResponseHandler(String[] allowedMethods, List<AdapterResponseInterceptor<Feed>> adapterResponseInterceptorList) {
        super(allowedMethods, adapterResponseInterceptorList);
    }

    @Override
    protected ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse <Feed> adapterResponse) {
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
                buildLinkHeader(responseContext, adapterResponse);
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

    protected ResponseContext buildLinkHeader(ResponseContext responseContext, AdapterResponse<Feed> adapterResponse) {
        if ( adapterResponse != null && adapterResponse.getBody() != null ) {

            String nextLink = null;
            if ( adapterResponse.getBody().getLink(Link.REL_NEXT) != null ) {
                nextLink = buildLinkHeaderForType(adapterResponse, Link.REL_NEXT);
            }
            else if ( adapterResponse.getBody().getLink( FeedSource.REL_ARCHIVE_NEXT ) != null ) {
                nextLink = buildLinkHeaderForType(adapterResponse, FeedSource.REL_ARCHIVE_NEXT );
            }

            String prevLink = null;
            if ( adapterResponse.getBody().getLink(Link.REL_PREVIOUS) != null ) {
                prevLink = buildLinkHeaderForType(adapterResponse, Link.REL_PREVIOUS);
            }
            else if ( adapterResponse.getBody().getLink( FeedSource.REL_ARCHIVE_PREV ) != null ) {
                prevLink = buildLinkHeaderForType(adapterResponse, FeedSource.REL_ARCHIVE_PREV );
            }

            if ( nextLink != null && prevLink != null ) {
                responseContext.addHeader(LINK_HEADER_NAME, nextLink + LINK_SEPARATOR + prevLink);
            } else if ( nextLink != null ) {
                responseContext.addHeader(LINK_HEADER_NAME, nextLink);
            } else if ( prevLink != null ) {
                responseContext.addHeader(LINK_HEADER_NAME, prevLink);
            }
        }
        return responseContext;
    }

    protected String buildLinkHeaderForType(AdapterResponse<Feed> adapterResponse, String linkType) {
        StringBuilder sb = new StringBuilder();
        sb.append(LT);
        sb.append(adapterResponse.getBody().getLink(linkType).getHref());
        sb.append(GT);
        sb.append("; rel=\"");
        sb.append(linkType);
        sb.append("\"");
        return sb.toString();
    }

    private boolean entityTagMatches(EntityTag[] ifNoneMatch, EntityTag entityTag) {
        if (ifNoneMatch == null || entityTag == null) {
            return false;
        }
        List<EntityTag> ifNoneMatchList = Arrays.asList(ifNoneMatch);
        return ifNoneMatchList.size() == 1 && ifNoneMatchList.get(0).getTag().equals(entityTag.getTag());
    }
}
