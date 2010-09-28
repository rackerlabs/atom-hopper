/*
 *  Copyright 2010 Rackspace.
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
package com.rackspace.cloud.sense.abdera;

import org.apache.abdera.model.Document;
import com.rackspace.cloud.sense.domain.response.EntryResponse;
import com.rackspace.cloud.sense.domain.feed.GetFeedRequest;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import java.util.HashMap;
import java.util.Map;
import org.apache.abdera.protocol.server.TargetType;
import com.rackspace.cloud.sense.util.RegexList;
import com.rackspace.cloud.sense.config.SenseFeedConfiguration;
import com.rackspace.cloud.sense.domain.entry.GetEntryRequest;
import com.rackspace.cloud.sense.domain.entry.PostEntryRequest;
import com.rackspace.cloud.sense.domain.entry.PutEntryRequest;
import com.rackspace.cloud.sense.domain.entry.SenseGetEntryRequest;
import com.rackspace.cloud.sense.domain.entry.SensePostEntryRequest;
import com.rackspace.cloud.sense.domain.entry.SensePutEntryRequest;
import com.rackspace.cloud.sense.domain.feed.SenseGetFeedRequest;
import com.rackspace.cloud.sense.domain.response.FeedResponse;
import org.apache.abdera.model.Entry;

import java.util.Calendar;
import java.util.Date;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.context.ResponseContextException;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

import static com.rackspace.cloud.sense.domain.http.HttpResponseCode.*;

/**
 *
 * @author John Hopper
 */
public class SenseFeedAdapter extends AbstractCollectionAdapter {

    private static final Calendar CALENDAR_INSTANCE = Calendar.getInstance();
    private final Abdera abdera;
    private final SenseFeedConfiguration feedConfig;
    private final RegexList feedTargets;
    private final FeedSourceAdapter configuredDatasourceAdapter;

    public SenseFeedAdapter(Abdera abdera, SenseFeedConfiguration feedConfig, FeedSourceAdapter configuredDatasourceAdapter) {
        this.abdera = abdera;
        this.feedConfig = feedConfig;

        this.configuredDatasourceAdapter = configuredDatasourceAdapter;

        feedTargets = new RegexList();
    }

    protected Feed newFeed() {
        return abdera.newFeed();
    }

    protected Entry newEntry() {
        return abdera.newEntry();
    }

    public SenseFeedConfiguration getFeedConfiguration() {
        return feedConfig;
    }

    public void addTargetRegex(String target) {
        feedTargets.add(target);
    }

    public boolean handles(String target) {
        return feedTargets.targets(target);
    }

    @Override
    public String getHref(RequestContext request) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("collection", "feed");

        return request.urlFor(TargetType.TYPE_COLLECTION, params);
    }

    @Override
    public String getAuthor(RequestContext rc) throws ResponseContextException {
        return getFeedConfiguration().getAuthor();
    }

    @Override
    public String getId(RequestContext rc) {
        final SenseFeedConfiguration config = getFeedConfiguration();

        return new StringBuilder("tag:").append(config.getFullUri()).append(",").append(CALENDAR_INSTANCE.get(Calendar.YEAR)).append(":").append(config.getBaseUrn()).toString();
    }

    @Override
    public String getTitle(RequestContext rc) {
        return getFeedConfiguration().getTitle();
    }

    @Override
    public ResponseContext deleteEntry(RequestContext rc) {
        return ProviderHelper.nocontent();
    }

    @Override
    public ResponseContext getEntry(RequestContext rc) {
        final String entityId = rc.getTarget().getParameter(TargetResolverField.ENTRY.name());
        final GetEntryRequest info = new SenseGetEntryRequest(rc, entityId);

        try {
            return handleEntryResponse(rc, configuredDatasourceAdapter.getEntry(info, newEntry()));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    private ResponseContext handleEntryResponse(RequestContext rc, EntryResponse response) {
        final Entry e = response.hasEntry() ? response.getEntry() : newEntry();

        switch (response.getResponseCode()) {
            case OK:
                return ProviderHelper.returnBase(e, response.getResponseCode().getRawHttpCode(), e.getUpdated());

            case CREATED:
                return ProviderHelper.returnBase(e, response.getResponseCode().getRawHttpCode(), e.getUpdated());

            case NOT_FOUND:
                return ProviderHelper.notfound(rc, response.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, response.getMessage(), new Exception());

            default:
                return ProviderHelper.notfound(rc);
        }
    }

    private ResponseContext handleFeedResponse(RequestContext rc, FeedResponse response) {
        final Feed f = response.hasFeed() ? response.getFeed() : newFeed();

        switch (response.getResponseCode()) {
            case OK:
                return ProviderHelper.returnBase(f, response.getResponseCode().getRawHttpCode(), f.getUpdated());

            case NOT_FOUND:
                return ProviderHelper.notfound(rc, response.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, response.getMessage(), new Exception());

            default:
                return ProviderHelper.notfound(rc);
        }
    }

    @Override
    public ResponseContext getFeed(RequestContext rc) {
        final GetFeedRequest feedInfo = new SenseGetFeedRequest(rc, rc.getTargetPath(), rc.getParameter(TargetResolverField.FEED.name()));

        try {
            return handleFeedResponse(rc, configuredDatasourceAdapter.getFeed(feedInfo, newFeed()));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext postEntry(RequestContext rc) {
        try {
            final Document<Entry> entryToPost = rc.getDocument();
            final PostEntryRequest postRequest = new SensePostEntryRequest(rc, entryToPost.getRoot());

            return handleEntryResponse(rc, configuredDatasourceAdapter.postEntry(postRequest));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext putEntry(RequestContext rc) {
        try {
            final Document<Entry> entryToPost = rc.getDocument();
            final PutEntryRequest putRequest = new SensePutEntryRequest(rc, entryToPost.getRoot(), rc.getParameter(TargetResolverField.ENTRY.name()));

            return handleEntryResponse(rc, configuredDatasourceAdapter.putEntry(putRequest));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }
}
