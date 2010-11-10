package com.rackspace.cloud.sense.client.adapter.impl;

import com.rackspace.cloud.sense.client.adapter.AdapterTools;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.client.adapter.ResponseBuilder;
import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import com.rackspace.cloud.util.logging.Logger;
import com.rackspace.cloud.util.logging.RCLogger;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

public class HashMapFeedAdapter implements FeedSourceAdapter {

    private static final Logger log = new RCLogger(HashMapFeedAdapter.class);
    
    private final Map<String, Entry> entries;
    private AdapterTools tools;
    private int count;

    public HashMapFeedAdapter() {
        entries = new HashMap<String, Entry>();
        count = 0;
    }

    @Override
    public void setAdapterTools(AdapterTools tools) {
        this.tools = tools;
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) {
        final Entry removedEntry = entries.remove(id);

        return removedEntry != null ? ResponseBuilder.ok() : ResponseBuilder.<EmptyBody>notFound();
    }

    @Override
    public AdapterResponse<Entry> getEntry(RequestContext request, String id) {
        final Entry e = entries.get(id);

        return e != null ? ResponseBuilder.found(e) : ResponseBuilder.<Entry>notFound();
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request) {
        final Feed f = tools.newFeed();

        for (Entry storedEntry : entries.values()) {
            f.addEntry(storedEntry);
        }

        return ResponseBuilder.found(f);
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request, String lastId) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> postEntry(RequestContext request, Entry e) {
        final Entry entry = e;

        entry.setUpdated(Calendar.getInstance().getTime());
        entries.put("" + (++count), entry);

        try {
            final IRI base = request.getBaseUri();
            entry.addLink(base.toURL().toString() + request.getTargetPath() + "/" + count);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }

        return ResponseBuilder.found(e);
    }

    @Override
    public AdapterResponse<Entry> putEntry(RequestContext request, String id, Entry e) {
        final Entry oldEntry = entries.get(id);

        if (oldEntry != null) {
            entries.put(id, e);
            return ResponseBuilder.found(e);
        }

        return ResponseBuilder.notFound();
    }
}
