package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.GenericAdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

public interface FeedSourceAdapter {

    void setAdapterTools(AdapterTools tools);

    GenericAdapterResponse<Feed> getFeed(RequestContext request) throws UnsupportedOperationException;

    GenericAdapterResponse<Entry> getEntry(RequestContext request, String id) throws UnsupportedOperationException;

    GenericAdapterResponse<Entry> postEntry(RequestContext request, Entry e) throws UnsupportedOperationException;

    GenericAdapterResponse<Entry> putEntry(RequestContext request, String id, Entry e) throws UnsupportedOperationException;

    GenericAdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) throws UnsupportedOperationException;
}
