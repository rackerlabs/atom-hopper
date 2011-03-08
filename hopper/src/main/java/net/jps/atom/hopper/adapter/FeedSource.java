/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.request.GetEntryRequest;
import net.jps.atom.hopper.adapter.request.GetFeedRequest;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public interface FeedSource {

    /**
     * Requests a single feed from the adapter. This request did not contain
     * paging information and the adapter may assume that the requester is
     * requesting the head of the feed.
     *
     * @param request
     * Raw request information.
     *
     * @return
     */
    AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest);

    /**
     * Requests a single entry from the adapter. This request is scoped by the
     * unique string ID of the entry being requested.
     *
     * @param request
     *
     * @return
     */
    AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest);

    /**
     * Provides internal SENSe systems with a get method for feeds that can be
     * scoped by a starting time and an ending time.
     * 
     * This method does not carry the guarantee that the feed returned will
     * represent the entire feed over the requested date range. Instead, the
     * internal system consumers of this method (archivers in the usual case)
     * will continue to request the feed while narrowing the date range with the
     * creation date of the last feed entry until a feed of zero length is
     * returned.
     * 
     * @param page
     * @param startingEntryDate
     * @param lastEntryDate
     * @return 
     */
    Feed getFeedByDateRange(Calendar startingEntryDate, Calendar lastEntryDate);
}
