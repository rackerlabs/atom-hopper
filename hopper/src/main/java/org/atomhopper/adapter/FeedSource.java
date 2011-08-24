package org.atomhopper.adapter;

import java.util.Calendar;
import org.atomhopper.adapter.request.GetCategoriesRequest;
import org.atomhopper.adapter.request.GetEntryRequest;
import org.atomhopper.adapter.request.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 * A feed source, as defined by this interface, is responsible for retrieving the
 * feed and its associated entry data.
 *
 * Note: this interface is required to serve the lossy variant of a feed (i.e. head)
 */
public interface FeedSource {

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

    /**
     * Retrieves a list of categories supported by this feed source.
     *
     * A workspace provider may poll all of its associated feeds for their
     * categories and then aggregate them into a service document.
     *
     * @return
     */
    AdapterResponse<Categories> getCategories(GetCategoriesRequest getCategoriesRequest);

    /**
     * Provides internal systems with a get method for feeds that can be scoped
     * by a starting time and an ending time.
     *
     * This method does not carry the guarantee that the feed returned will
     * represent the entire feed over the requested date range. Instead, the
     * internal system consumers of this method (archivers in the usual case)
     * will continue to request the feed while narrowing the date range with the
     * creation date of the last feed entry until a feed of zero length is
     * returned.
     *
     * @param startingEntryDate
     * @param lastEntryDate
     * @return
     */
    Feed getFeedByDateRange(Calendar startingEntryDate, Calendar lastEntryDate);
}
