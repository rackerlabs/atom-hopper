package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

public interface FeedSourceAdapter {

    void setAdapterTools(AdapterTools tools);

    AdapterResponse<Feed> getFeed(RequestContext request) throws UnsupportedOperationException;

    AdapterResponse<Feed> getFeed(RequestContext request, String lastId) throws UnsupportedOperationException;

    AdapterResponse<Entry> getEntry(RequestContext request, String id) throws UnsupportedOperationException;

    AdapterResponse<Entry> postEntry(RequestContext request, Entry e) throws UnsupportedOperationException;

    AdapterResponse<Entry> putEntry(RequestContext request, String id, Entry e) throws UnsupportedOperationException;

    AdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) throws UnsupportedOperationException;
}
