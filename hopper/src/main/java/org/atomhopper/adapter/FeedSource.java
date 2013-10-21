package org.atomhopper.adapter;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;

import java.net.URL;

/**
 * A feed source, as defined by this interface, is responsible for retrieving the
 * feed and its associated entry data.
 *
 */
public interface FeedSource extends AtomHopperAdapter {

    public static final String REL_ARCHIVE_NEXT = "next-archive";
    public static final String REL_ARCHIVE_PREV = "prev-archive";

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

    /**
     * Set's the current source as an archive feed.  An archive feed must have a link to
     * the current feed which its the archive for.  By setting the current feed for this source,
     * you are declaring it as an archive feed.
     *
     * @param urlCurrent The URL to the current feed for this archive.  This will be displayed
     *                   in the "current" link
     */
    public void setAsArchived( URL urlCurrent );

    /**
     * If an atom feed has a corresponding archive,
     *
     * @param url
     */
    public void setArchiveUrl( URL url );
}
