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
import org.apache.abdera.protocol.server.TargetType;

import net.jps.atom.hopper.response.EmptyBody;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.ResponseParameter;
import org.apache.abdera.model.Entry;

import java.util.Date;
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

    public FeedAdapter(FeedConfiguration feedConfig, FeedSourceAdapter configuredDatasourceAdapter) {
        this.feedConfig = feedConfig;
        this.configuredDatasourceAdapter = configuredDatasourceAdapter;

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
        final String marker = rc.getParameter(ResponseParameter.MARKER.toString());

        try {
            if (!StringUtilities.isBlank(marker)) {
                return handleFeedResponse(rc, configuredDatasourceAdapter.getFeed(rc, marker));
            } else {
                return handleFeedResponse(rc, configuredDatasourceAdapter.getFeed(rc));
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

                if (!StringUtilities.isBlank(returnedEntryCopy.getId().toString())) {
                    if (returnedEntryCopy.getLinks("self").isEmpty()) {
//                        returnedEntryCopy.addLink(, null)
                    }
                } else {
                    log.warn("New ID for Entry Update was not returned. Please verify that your adapter sets the entry's ID field");
                }
            }

            return handleEntryResponse(rc, response);
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

            return handleEntryResponse(request, response);
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
            return handleEntryResponse(rc, configuredDatasourceAdapter.getEntry(rc, entityId));
        } catch (Exception ex) {
            return ProviderHelper.servererror(rc, ex.getMessage(), ex);
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

        addPagingLinks(rc, response);

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

    private void addPagingLinks(RequestContext rc, AdapterResponse<Feed> response) {
        final Feed f = response.getBody();
        final int numEntries = f.getEntries().size();
        final String nextMarker = f.getEntries().get(numEntries).getId().toString(), previousMarker = f.getEntries().get(0).getId().toString();

        final String self = StringUtilities.join(rc.getBaseUri().toString(), rc.getTargetPath());

        // Add markers
        f.addLink(StringUtilities.join(self), "current");
        f.addLink(StringUtilities.join(self, "?marker=", nextMarker), "next");
        f.addLink(StringUtilities.join(self, "?marker=", previousMarker), "prev");
    }

    private String selfUriString(RequestContext rc) {
        return rc.getBaseUri().toString() + rc.getTargetPath();
    }
    
    private void addArchiveLinks(RequestContext rc, AdapterResponse<Feed> response) {
//        final String self = selfUriString(rc);
//        final Feed f = response.getBody();
//        
//        if (feedConfig.getArchive() != null) {
//            f.addLink(StringUtilities.join(self, "?marker=", previousMarker), "prev");
//        }
    }
}
