package org.atomhopper.abdera.filter;

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.util.EntityTag;
import org.atomhopper.response.AdapterResponse;

/**
 * AdapterResponseProcessor for a Feed that adds a weak entity tag to the feed using
 * the entry id of the first entry in the feed.
 */
public class FeedEntityTagProcessor implements AdapterResponseInterceptor<Feed> {

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();

        final int totalEntries = f.getEntries().size();

        // If there are no entries in the feed
        if (totalEntries == 0) {
            return;
        }

        // Get the id of the first entry on this page
        String id = f.getEntries().get(0).getId().toString();
        // Get the id of the last entry on this page
        String lastId = f.getEntries().get(totalEntries-1).getId().toString();

        EntityTag feedEtag = new EntityTag(id + ":" + lastId, true);
        adapterResponse.setEntityTag(feedEtag);
    }

}
