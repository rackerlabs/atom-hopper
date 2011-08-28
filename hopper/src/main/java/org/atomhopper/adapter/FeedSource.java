package org.atomhopper.adapter;

import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 * A feed source, as defined by this interface, is responsible for retrieving the
 * feed and its associated entry data.
 *
 */
public interface FeedSource extends AtomHopperAdapter {

    FeedInformation getFeedInformation();

    /**
     * Requests a single feed from the adapter. This request did not contain
     * paging information and the adapter may assume that the requester is
     * requesting the head of the feed.
     *
     * @param request
     * @see GetEntryRequest
     *
     * @return
     */
    AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest);

    /**
     * Requests a single entry from the adapter.
     *
     * @param request
     * @see GetEntryRequest
     *
     * @return
     */
    AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest);
}
