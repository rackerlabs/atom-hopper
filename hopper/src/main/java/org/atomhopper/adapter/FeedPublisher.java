package org.atomhopper.adapter;

import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;

/**
 * A feed publisher, as defined by this interface, is responsible for committing
 * client change requests to the feed it represents.
 * 
 * Note: this interface is required for ATOMpub functionality
 */
public interface FeedPublisher extends AtomHopperAdapter {

    /**
     * Requests a single entry be added to the feed.
     *
     * @param postEntryRequest
     * @see PostEntryRequest
     * 
     * @return
     * The returned entry should contain all of the information a client would
     * need to then request the newly added entry. This should include linking 
     * and identifying the new entry in the response
     */
    AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest);

    /**
     * Requests that an entry be updated. This request is scoped by the unique
     * string ID of the entry the update is being requested for.
     *
     * @param putEntryRequest
     * @see PutEntryRequest
     * 
     * @return
     */
    AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest);

    /**
     * Requests that an entry be deleted. This request is scoped by the unique
     * string ID of the entry the delete is being requested for.
     *
     * @param deleteEntryRequest
     * @see DeleteEntryRequest
     *
     * @return
     */
    AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest);
}
