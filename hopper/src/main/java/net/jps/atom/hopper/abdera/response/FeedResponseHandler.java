/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.abdera.response;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import net.jps.atom.hopper.abdera.filter.AdapterResponseProcessor;
import net.jps.atom.hopper.abdera.filter.FeedPagingProcessor;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

/**
 *
 * @author zinic
 */
public class FeedResponseHandler implements ResponseHandler<Feed> {

    final List<AdapterResponseProcessor<Feed>> feedProcessors;

    public FeedResponseHandler() {
        feedProcessors = new LinkedList<AdapterResponseProcessor<Feed>>();
        feedProcessors.add(new FeedPagingProcessor());
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
                return ProviderHelper.servererror(rc, adapterResponse.getMessage(), new Exception());

            default:
                return ProviderHelper.notfound(rc);
        }
    }
    
    private void processResponse(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        for (AdapterResponseProcessor<Feed> processor : feedProcessors) {
            processor.process(rc, adapterResponse);
        }
    }
}
