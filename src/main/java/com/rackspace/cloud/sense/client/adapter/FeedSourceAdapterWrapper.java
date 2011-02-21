package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import com.rackspace.cloud.sense.domain.response.EmptyBody;
import java.util.Calendar;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

public class FeedSourceAdapterWrapper implements FeedSourceAdapter {

    private FeedSourceAdapter feedSourceAdapter;

    public final void setFeedSourceAdapter(FeedSourceAdapter feedSourceAdapter) {
        this.feedSourceAdapter = feedSourceAdapter;
    }

    public final FeedSourceAdapter getFeedSourceAdapter() {
        return feedSourceAdapter;
    }

    @Override
    public void setAdapterTools(AdapterTools tools) {
        feedSourceAdapter.setAdapterTools(tools);
    }

    @Override
    public AdapterResponse<Entry> putEntry(RequestContext request, String entryId, Entry entryToUpdate) throws UnsupportedOperationException {
        return feedSourceAdapter.putEntry(request, entryId, entryToUpdate);
    }

    @Override
    public AdapterResponse<Entry> postEntry(RequestContext request, Entry entryToAdd) throws UnsupportedOperationException {
        return feedSourceAdapter.postEntry(request, entryToAdd);
    }

    @Override
    public AdapterResponse<Feed> getFeedPage(RequestContext request, int page, String markerId) throws UnsupportedOperationException {
        return feedSourceAdapter.getFeedPage(request, page, markerId);
    }

    @Override
    public AdapterResponse<Feed> getFeed(Calendar lastEntry) throws UnsupportedOperationException {
        return feedSourceAdapter.getFeed(lastEntry);
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request) throws UnsupportedOperationException {
        return feedSourceAdapter.getFeed(request);
    }

    @Override
    public AdapterResponse<Entry> getEntry(RequestContext request, String entryId) throws UnsupportedOperationException {
        return feedSourceAdapter.getEntry(request, entryId);
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) throws UnsupportedOperationException {
        return feedSourceAdapter.deleteEntry(request, id);
    }
}
