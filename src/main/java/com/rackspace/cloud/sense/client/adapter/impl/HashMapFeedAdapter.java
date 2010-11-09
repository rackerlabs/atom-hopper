/*
 *  Copyright 2010 zinic.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense.client.adapter.impl;

import com.rackspace.cloud.sense.client.adapter.AdapterTools;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.client.adapter.ResponseBuilder;
import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.GenericAdapterResponse;
import com.rackspace.cloud.util.logging.Logger;
import com.rackspace.cloud.util.logging.RCLogger;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class HashMapFeedAdapter implements FeedSourceAdapter {

    private static final Logger log = new RCLogger("HashMapFeedAdapter", "com.rackspace.cloud.sense.client.adapter.impl");
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
    public GenericAdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) {
        final Entry removedEntry = entries.remove(id);

        return removedEntry != null ? ResponseBuilder.ok() : ResponseBuilder.<EmptyBody>notFound();
    }

    @Override
    public GenericAdapterResponse<Entry> getEntry(RequestContext request, String id) {
        final Entry e = entries.get(id);

        return e != null ? ResponseBuilder.found(e) : ResponseBuilder.<Entry>notFound();
    }

    @Override
    public GenericAdapterResponse<Feed> getFeed(RequestContext request) {
        final Feed f = tools.newFeed();

        for (Entry storedEntry : entries.values()) {
            f.addEntry(storedEntry);
        }

        return ResponseBuilder.found(f);
    }

    @Override
    public GenericAdapterResponse<Entry> postEntry(RequestContext request, Entry e) {
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
    public GenericAdapterResponse<Entry> putEntry(RequestContext request, String id, Entry e) {
        final Entry oldEntry = entries.get(id);

        if (oldEntry != null) {
            entries.put(id, e);
            return ResponseBuilder.found(e);
        }

        return ResponseBuilder.notFound();
    }
}
