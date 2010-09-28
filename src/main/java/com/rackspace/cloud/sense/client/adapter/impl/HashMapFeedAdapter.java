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

import com.rackspace.cloud.sense.domain.entry.PutEntryRequest;
import com.rackspace.cloud.sense.domain.response.EntryResponse;
import com.rackspace.cloud.sense.domain.response.FeedResponse;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.domain.entry.GetEntryRequest;
import com.rackspace.cloud.sense.domain.entry.PostEntryRequest;
import com.rackspace.cloud.sense.domain.feed.GetFeedRequest;
import com.rackspace.cloud.sense.util.StaticLoggingFacade;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

import static com.rackspace.cloud.sense.client.adapter.ResponseBuilder.*;

/**
 *
 * @author zinic
 */
public class HashMapFeedAdapter implements FeedSourceAdapter {

    private final Map<String, Entry> entries;
    private int count;

    public HashMapFeedAdapter() {
        entries = new HashMap<String, Entry>();
        count = 0;
    }

    @Override
    public EntryResponse getEntry(GetEntryRequest entryInfo, Entry copy) {
        if (entries.containsKey(entryInfo.getEntryId())) {
            return ok(entries.get(entryInfo.getEntryId()));
        }

        return notFound("Entry ", entryInfo.getEntryId(), " not found.");
    }

    @Override
    public FeedResponse getFeed(GetFeedRequest feedInfo, Feed copy) {
        for (Entry storedEntry : entries.values()) {
            copy.addEntry(storedEntry);
        }

        return ok(copy);
    }

    @Override
    public EntryResponse postEntry(PostEntryRequest postRequest) {
        final Entry entry = postRequest.getEntryToPost();

        entry.setUpdated(Calendar.getInstance().getTime());
        entries.put("" + (++count), entry);

        try {
            final RequestContext requestContext = postRequest.getRequestContext();

            final IRI base = requestContext.getBaseUri();

            entry.addLink(base.toURL().toString() + requestContext.getTargetPath() + "/" + count);
        } catch (Exception ex) {
            StaticLoggingFacade.logFatal(ex.getMessage());
        }

        return created(entry);
    }

    @Override
    public EntryResponse putEntry(PutEntryRequest putRequest) {
        if (entries.containsKey(putRequest.getEntryId())) {
            entries.put(putRequest.getEntryId(), putRequest.getEntryToUpdate());

            return ok();
        }

        return notFound("Entry ", putRequest.getEntryId(), " not found.");
    }
}
