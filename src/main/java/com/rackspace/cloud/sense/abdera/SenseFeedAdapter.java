package com.rackspace.cloud.sense.abdera;

import com.rackspace.cloud.sense.config.v1_0.FeedConfig;
import org.apache.abdera.model.Document;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import java.util.HashMap;
import java.util.Map;
import org.apache.abdera.protocol.server.TargetType;
import com.rackspace.cloud.sense.util.RegexList;

import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import com.rackspace.cloud.sense.domain.response.ResponseParameter;
import com.rackspace.cloud.util.StringUtilities;
import com.rackspace.cloud.util.http.HttpStatusCode;
import com.rackspace.cloud.util.logging.Logger;
import com.rackspace.cloud.util.logging.RCLogger;
import org.apache.abdera.model.Entry;

import java.util.Date;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

public class SenseFeedAdapter extends AbstractCollectionAdapter {

    private static final Logger log = new RCLogger(SenseFeedAdapter.class);

    private final FeedConfig feedConfig;
    private final RegexList feedTargets;
    private final FeedSourceAdapter configuredDatasourceAdapter;
    private final FeedChangeTracker changeTracker;

    public SenseFeedAdapter(FeedConfig feedConfig, FeedSourceAdapter configuredDatasourceAdapter) {
        this.feedConfig = feedConfig;
        this.configuredDatasourceAdapter = configuredDatasourceAdapter;

        changeTracker = new FeedChangeTracker();
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
        final String pageRequested = rc.getParameter(ResponseParameter.PAGE.toString());
        final String lastEntryId = rc.getParameter(ResponseParameter.MARKER.toString());

        try {
            if (!StringUtilities.isBlank(pageRequested)) {
                return handleFeedResponse(rc, configuredDatasourceAdapter.getFeed(rc, Integer.parseInt(pageRequested), lastEntryId));
            } else {
                return handleFeedResponse(rc, configuredDatasourceAdapter.getFeed(rc));
            }
        } catch (UnsupportedOperationException uoe) {
            return ProviderHelper.notallowed(rc, uoe.getMessage(), new String[0]); //TODO: Fix this var-args bullshit
        } catch (NumberFormatException nfe) {
            return ProviderHelper.badrequest(rc, "Page requested is not a number");
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext postEntry(RequestContext rc) {
        try {
            final Document<Entry> entryToPost = rc.getDocument();
            final AdapterResponse<Entry> response = configuredDatasourceAdapter.postEntry(rc, entryToPost.getRoot());

            if (response.getResponseStatus() == HttpStatusCode.CREATED) {
                final String entryId = response.getParameter(ResponseParameter.ENTRY_ID);

                if (!StringUtilities.isBlank(entryId)) {
                    changeTracker.putEntry(entryId, response.getBody());
                } else {
                    //TODO: Log that an entry id was not returned
                }
            }

            return handleEntryResponse(rc, response);
        } catch (Throwable t) {
            return ProviderHelper.servererror(rc, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext putEntry(RequestContext request) {
        try {
            final Document<Entry> entryToUpdate = request.getDocument();
            final String entryId = request.getParameter(TargetResolverField.ENTRY.name());

            final AdapterResponse<Entry> response = configuredDatasourceAdapter.putEntry(request, entryId, entryToUpdate.getRoot());

            if (response.getResponseStatus() == HttpStatusCode.OK) {
                changeTracker.putEntry(entryId, response.getBody());
            }

            return handleEntryResponse(request, response);
        } catch (Throwable t) {
            return ProviderHelper.servererror(request, t.getMessage(), t);
        }
    }

    @Override
    public ResponseContext deleteEntry(RequestContext rc) {
        final String entryId = rc.getTarget().getParameter(TargetResolverField.ENTRY.name());

        try {
            final AdapterResponse<EmptyBody> response = configuredDatasourceAdapter.deleteEntry(rc, entryId);

            if (response.getResponseStatus() == HttpStatusCode.OK) {
                changeTracker.removeEntry(entryId);
            }

            return handleEmptyResponse(rc, response);
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

    private ResponseContext handleEmptyResponse(RequestContext rc, AdapterResponse<EmptyBody> response) {
        switch (response.getResponseStatus()) {
            case NOT_FOUND:
                return ProviderHelper.notfound(rc, response.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, response.getMessage(), new Exception());

            default:
                return ProviderHelper.nocontent();
        }
    }

    private ResponseContext handleEntryResponse(RequestContext rc, AdapterResponse<Entry> response) {
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

    private ResponseContext handleFeedResponse(RequestContext rc, AdapterResponse<Feed> response) {
        final Date lastUpdated = response.getBody() != null ? response.getBody().getUpdated() : null;

        addPaginationInformationToFeed(rc, response);

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

    private void addPaginationInformationToFeed(RequestContext rc, AdapterResponse<Feed> response) {
        final String page = response.getParameter(ResponseParameter.PAGE);
        final String marker = response.getParameter(ResponseParameter.MARKER);

        final Feed f = response.getBody();

        if (page != null) {
            final String selfMinusPageNumber = StringUtilities.join(rc.getBaseUri().toString(), rc.getTargetPath(), "?", ResponseParameter.PAGE.toString(), "=");

            f.addLink(marker != null ? StringUtilities.join(selfMinusPageNumber, page, "&", ResponseParameter.MARKER.toString(), "=", marker) : selfMinusPageNumber + page, "self");


            final int pageNumber = Integer.parseInt(page);
            f.addLink(selfMinusPageNumber + (pageNumber + 1), "next");

            //Add previous
            if (pageNumber - 1 > 0) {
                f.addLink(selfMinusPageNumber + (pageNumber - 1), "previous");
            }
        }
    }
}
