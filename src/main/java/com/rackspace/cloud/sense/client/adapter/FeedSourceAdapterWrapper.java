package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import com.rackspace.cloud.sense.domain.response.EmptyBody;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

public class FeedSourceAdapterWrapper implements FeedSourceAdapter {
    private final FeedSourceAdapter internalAdapter;

    public FeedSourceAdapterWrapper(FeedSourceAdapter internalAdapter) {
        this.internalAdapter = internalAdapter;
    }

    public final FeedSourceAdapter getFeedSourceAdapter() {
        return internalAdapter;
    }

    @Override
    public void setAdapterTools(AdapterTools tools) {
        internalAdapter.setAdapterTools(tools);
    }

    @Override
    public AdapterResponse<Entry> putEntry(RequestContext request, String entryId, Entry entryToUpdate) throws UnsupportedOperationException {
        return internalAdapter.putEntry(request, entryId, entryToUpdate);
    }

    @Override
    public AdapterResponse<Entry> postEntry(RequestContext request, Entry entryToAdd) throws UnsupportedOperationException {
        return internalAdapter.postEntry(request, entryToAdd);
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request, int page, String markerId) throws UnsupportedOperationException {
        return internalAdapter.getFeed(request, page, markerId);
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request) throws UnsupportedOperationException {
        return internalAdapter.getFeed(request);
    }

    @Override
    public AdapterResponse<Entry> getEntry(RequestContext request, String entryId) throws UnsupportedOperationException {
        return internalAdapter.getEntry(request, entryId);
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) throws UnsupportedOperationException {
        return internalAdapter.deleteEntry(request, id);
    }
}
