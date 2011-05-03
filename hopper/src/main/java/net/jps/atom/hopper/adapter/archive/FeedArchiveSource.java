package net.jps.atom.hopper.adapter.archive;

import net.jps.atom.hopper.adapter.request.GetFeedArchiveRequest;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;

/**
 * A feed archive source, as defined by this interface, is responsible for
 * retrieving the archived (static, unchanging) variant of a feed. Its operation
 * life-cycle is analogous to a @see FeedSource
 * 
 * Note: this interface is required to serve the archived variant of a feed
 */
public interface FeedArchiveSource {

    /**
     * Requests an archived version of the feed.
     * 
     * @param getFeedArchiveRequest
     *
     * @return
     */
    AdapterResponse<Feed> getFeed(GetFeedArchiveRequest getFeedArchiveRequest);
}
