package com.rackspace.cloud.sense.abdera;

import com.rackspace.cloud.sense.config.v1_0.FeedConfig;
import org.apache.abdera.model.Document;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import java.util.HashMap;
import java.util.Map;
import org.apache.abdera.protocol.server.TargetType;
import com.rackspace.cloud.sense.util.RegexList;

import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.GenericAdapterResponse;
import org.apache.abdera.model.Entry;

import java.util.Date;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

public class SenseFeedAdapter extends AbstractCollectionAdapter {

    //TODO: Consider removing abdera reference
    private final Abdera abdera;

    private final FeedConfig feedConfig;
    private final RegexList feedTargets;
    private final FeedSourceAdapter configuredDatasourceAdapter;

    public SenseFeedAdapter(Abdera abdera, FeedConfig feedConfig, FeedSourceAdapter configuredDatasourceAdapter) {
        this.abdera = abdera;
        this.feedConfig = feedConfig;

        this.configuredDatasourceAdapter = configuredDatasourceAdapter;

        feedTargets = new RegexList();
    }

    public FeedConfig getFeedConfiguration() {
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
    public String getAuthor(RequestContext rc) {
        return getFeedConfiguration().getAuthor().getName();
    }

    @Override
    //TODO: Reimplement this
    public String getId(RequestContext rc) {
//        return new StringBuilder("tag:").append(feedConfig.getFullUri()).append(",").append(CALENDAR_INSTANCE.get(Calendar.YEAR)).append(":").append(config.getBaseUrn()).toString();
        return "TODO: ID";
    }

    @Override
    public String getTitle(RequestContext rc) {
        return getFeedConfiguration().getTitle();
    }

    @Override
    public ResponseContext getFeed(RequestContext rc) {
        try {
            return handleFeedResponse(rc, configuredDatasourceAdapter.getFeed(rc));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext postEntry(RequestContext rc) {
        try {
            final Document<Entry> entryToPost = rc.getDocument();

            return handleEntryResponse(rc, configuredDatasourceAdapter.postEntry(rc, entryToPost.getRoot()));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext putEntry(RequestContext rc) {
        try {
            final Document<Entry> entryToUpdate = rc.getDocument();

            return handleEntryResponse(rc, configuredDatasourceAdapter.putEntry(rc, rc.getParameter(TargetResolverField.ENTRY.name()), entryToUpdate.getRoot()));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext deleteEntry(RequestContext rc) {
        final String entityId = rc.getTarget().getParameter(TargetResolverField.ENTRY.name());

        try {
            return handleEmptyResponse(rc, configuredDatasourceAdapter.deleteEntry(rc, entityId));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext getEntry(RequestContext rc) {
        final String entityId = rc.getTarget().getParameter(TargetResolverField.ENTRY.name());

        try {
            return handleEntryResponse(rc, configuredDatasourceAdapter.getEntry(rc, entityId));
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    private ResponseContext handleEmptyResponse(RequestContext rc, GenericAdapterResponse<EmptyBody> response) {
        switch (response.getResponseStatus()) {
            case NOT_FOUND:
                return ProviderHelper.notfound(rc, response.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, response.getMessage(), new Exception());

            default:
                return ProviderHelper.nocontent();
        }
    }

    private ResponseContext handleEntryResponse(RequestContext rc, GenericAdapterResponse<Entry> response) {
        final Date lastUpdated = response.getBody() != null ? response.getBody().getUpdated() : null;

        switch (response.getResponseStatus()) {
            case OK:
            case CREATED:
                return ProviderHelper.returnBase(response.getBody(), response.getResponseStatus().intValue(), lastUpdated);

            case NOT_FOUND:
                return ProviderHelper.notfound(rc, response.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, response.getMessage(), new Exception());

            default:
                return ProviderHelper.notfound(rc);
        }
    }

    private ResponseContext handleFeedResponse(RequestContext rc, GenericAdapterResponse<Feed> response) {
        final Date lastUpdated = response.getBody() != null ? response.getBody().getUpdated() : null;

        switch (response.getResponseStatus()) {
            case OK:
                return ProviderHelper.returnBase(response.getBody(), response.getResponseStatus().intValue(), lastUpdated);

            case NOT_FOUND:
                return ProviderHelper.notfound(rc, response.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, response.getMessage(), new Exception());

            default:
                return ProviderHelper.notfound(rc);
        }
    }
}
