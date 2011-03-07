package net.jps.atom.hopper.abdera;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.util.RegexList;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.http.HttpStatusCode;
import org.apache.abdera.model.Document;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import java.util.HashMap;
import java.util.Map;
import net.jps.atom.hopper.abdera.response.StaticFeedResponseHandler;
import net.jps.atom.hopper.abdera.response.ResponseHandler;
import net.jps.atom.hopper.abdera.response.StaticEntryResponseHandler;
import org.apache.abdera.protocol.server.TargetType;

import net.jps.atom.hopper.response.EmptyBody;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.ResponseParameter;
import org.apache.abdera.model.Entry;

import net.jps.atom.hopper.config.v1_0.FeedConfiguration;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.impl.AbstractCollectionAdapter;

public class FeedAdapter extends AbstractCollectionAdapter {

    private static final Logger log = new RCLogger(FeedAdapter.class);
    private final FeedConfiguration feedConfig;
    private final RegexList feedTargets;
    private final FeedSourceAdapter configuredDatasourceAdapter;
    private final ResponseHandler<Feed> feedResponseHandler;
    private final ResponseHandler<Entry> entryResponseHandler;

    public FeedAdapter(FeedConfiguration feedConfig, FeedSourceAdapter configuredDatasourceAdapter) {
        this.feedConfig = feedConfig;
        this.configuredDatasourceAdapter = configuredDatasourceAdapter;

        feedResponseHandler = new StaticFeedResponseHandler();
        entryResponseHandler = new StaticEntryResponseHandler();

        feedTargets = new RegexList();
    }

    public FeedConfiguration getFeedConfiguration() {
        return feedConfig;
    }

    public void addTargetRegex(String target) {
        feedTargets.add(target);
    }

    public boolean handles(String target) {
        return feedTargets.matches(target) != null;
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
    //TODO: Reimplement this - getting there
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
        final String marker = rc.getParameter(ResponseParameter.MARKER.toString());

        try {
            if (!StringUtilities.isBlank(marker)) {
                return feedResponseHandler.handleAdapterResponse(rc, configuredDatasourceAdapter.getFeed(rc, marker));
            } else {
                return feedResponseHandler.handleAdapterResponse(rc, configuredDatasourceAdapter.getFeed(rc));
            }
        } catch (UnsupportedOperationException uoe) {
            return ProviderHelper.notallowed(rc, uoe.getMessage(), new String[0]); //TODO: Fix this var-args bullshit
        } catch (Exception ex) {
            return ProviderHelper.servererror(rc, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext postEntry(RequestContext rc) {
        try {
            final Document<Entry> entryToPost = rc.getDocument();
            final AdapterResponse<Entry> response = configuredDatasourceAdapter.postEntry(rc, entryToPost.getRoot());

            if (response.getResponseStatus() == HttpStatusCode.CREATED) {
                final Entry returnedEntryCopy = response.getBody();

                //TODO: Push into filter
                if (StringUtilities.isBlank(returnedEntryCopy.getId().toString())) {
                    log.warn("New ID for Entry Update was not returned. Please verify that your adapter sets the entry's ID field");
                }
            }

            return entryResponseHandler.handleAdapterResponse(rc, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(rc, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext putEntry(RequestContext request) {
        try {
            final Document<Entry> entryToUpdate = request.getDocument();
            final String entryId = request.getParameter(TargetResolverField.ENTRY.name());

            final AdapterResponse<Entry> response = configuredDatasourceAdapter.putEntry(request, entryId, entryToUpdate.getRoot());

            return entryResponseHandler.handleAdapterResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext deleteEntry(RequestContext rc) {
        final String entryId = rc.getTarget().getParameter(TargetResolverField.ENTRY.name());

        try {
            final AdapterResponse<EmptyBody> response = configuredDatasourceAdapter.deleteEntry(rc, entryId);

            return handleEmptyResponse(rc, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(rc, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext getEntry(RequestContext rc) {
        final String entityId = rc.getTarget().getParameter(TargetResolverField.ENTRY.name());

        try {
            return entryResponseHandler.handleAdapterResponse(rc, configuredDatasourceAdapter.getEntry(rc, entityId));
        } catch (Exception ex) {
            return ProviderHelper.servererror(rc, ex.getMessage(), ex);
        }
    }

    public static ResponseContext handleEmptyResponse(RequestContext rc, AdapterResponse<EmptyBody> response) {
        switch (response.getResponseStatus()) {
            case NOT_FOUND:
                return ProviderHelper.notfound(rc, response.getMessage());

            case INTERNAL_SERVER_ERROR:
                return ProviderHelper.servererror(rc, response.getMessage(), new Exception());

            default:
                return ProviderHelper.nocontent();
        }
    }
}
