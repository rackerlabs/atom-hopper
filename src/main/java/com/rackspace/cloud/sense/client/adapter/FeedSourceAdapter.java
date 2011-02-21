package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import java.util.Calendar;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 * A FeedSourceAdapter represents all of the operations related to a feed and its
 * associated entries.
 *
 * @author John Hopper
 */
public interface FeedSourceAdapter {

    /**
     * SENSe will inject adapters with a tools object using this method during
     * initialization of the service.
     *
     * @param tools
     */
    void setAdapterTools(AdapterTools tools);

    AdapterResponse<Feed> getFeed(Calendar lastEntry) throws UnsupportedOperationException;

    /**
     * Requests a single feed from the adapter. The request does not contain
     * paging information and the adapter may assume that the requester is
     * requesting the head of the feed.
     *
     * @param request
     *
     * @return
     *
     * @throws UnsupportedOperationException
     * Adapters may throw UnsupportOperationExceptions if they do not support the operation.
     */
    AdapterResponse<Feed> getFeed(RequestContext request) throws UnsupportedOperationException;

    /**
     * Requests a single feed from the adapter. This request is scoped by both
     * the page the request is asking for as well as a unique string ID that
     * represents a domain specific marker in the feed.
     *
     * @param request
     *
     * @param page
     * Number of the page being requested.
     *
     * @param markerId
     * Unique ID of the marker which may be interpreted by the adapter in a
     * domain specific way. The marker may be null if it is not set.
     *
     * @return
     *
     * @throws UnsupportedOperationException
     * Adapters may throw UnsupportOperationExceptions if they do not support the operation
     */
    AdapterResponse<Feed> getFeedPage(RequestContext request, int page, String markerId) throws UnsupportedOperationException;


    /**
     * Requests a single entry from the adapter. This request is scoped by the
     * unique string ID of the entry being requested.
     *
     * @param request
     *
     * @param entryId
     * Unique string ID of the entry being requested.
     *
     * @return
     *
     * @throws UnsupportedOperationException
     * Adapters may throw UnsupportOperationExceptions if they do not support the operation.
     */
    AdapterResponse<Entry> getEntry(RequestContext request, String entryId) throws UnsupportedOperationException;

    /**
     * Requests a single entry be added to the feed.
     *
     * @param request
     *
     * @param entryToAdd
     *
     * @return
     * The returned entry should contain all of the information a client would
     * need to then request the newly added entry.
     *
     * @throws UnsupportedOperationException
     * Adapters may throw UnsupportOperationExceptions if they do not support the operation.
     */
    AdapterResponse<Entry> postEntry(RequestContext request, Entry entryToAdd) throws UnsupportedOperationException;

    /**
     * Requests that an entry be updated. This request is scoped by the unique
     * string ID of the entry the update is being requested for.
     *
     * @param request
     *
     * @param entryId
     *
     * @param entryToUpdate
     *
     * @return
     * The returned entry should contain all updated information including:
     * hrefs, datestamps and content.
     *
     * @throws UnsupportedOperationException
     * Adapters may throw UnsupportOperationExceptions if they do not support the operation.
     */
    AdapterResponse<Entry> putEntry(RequestContext request, String entryId, Entry entryToUpdate) throws UnsupportedOperationException;

    /**
     * Requests that an entry be deleted. This request is scoped by the unique
     * string ID of the entry the delete is being requested for.
     *
     * @param request
     *
     * @param id
     *
     * @return
     *
     * @throws UnsupportedOperationException
     * Adapters may throw UnsupportOperationExceptions if they do not support the operation.
     */
    AdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) throws UnsupportedOperationException;
}
