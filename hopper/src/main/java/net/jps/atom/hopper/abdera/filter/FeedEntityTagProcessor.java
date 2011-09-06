package net.jps.atom.hopper.abdera.filter;

import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.util.EntityTag;

/**
 * AdapterResponseProcessor for a Feed that adds a weak entity tag to the feed using
 * the entry id of the first entry in the feed.
 */
public class FeedEntityTagProcessor implements AdapterResponseProcessor<Feed> {

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();

        // If there are no entries in the feed
        if (f.getEntries().size() == 0) {
            return;
        }

        // Get the id of the first entry on this page
        String id = f.getEntries().get(0).getId().toString();
        EntityTag feedEtag = new EntityTag(id, true);
        adapterResponse.setEntityTag(feedEtag);
    }

}
