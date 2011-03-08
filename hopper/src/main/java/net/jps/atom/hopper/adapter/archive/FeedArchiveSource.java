package net.jps.atom.hopper.adapter.archive;

import net.jps.atom.hopper.adapter.request.GetFeedArchiveRequest;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;

/**
 * This interface describes an adapter that is able to archive and store feeds as
 * well as retrieve them. This interface may be tagged onto a FeedSourceAdapter
 * to combine the Functionality of both in one class.
 *
 * @author John Hopper
 */
public interface FeedArchiveSource {

    /**
     * Requests an archived version of the feed.
     * 
     * @param request
     * @param date
     * 
     * @return
     */
    AdapterResponse<Feed> getFeed(GetFeedArchiveRequest getFeedArchiveRequest);
}
