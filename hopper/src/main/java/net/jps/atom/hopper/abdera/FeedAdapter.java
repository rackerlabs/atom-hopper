package net.jps.atom.hopper.abdera;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.http.HttpStatusCode;
import java.util.HashMap;
import java.util.Map;
import net.jps.atom.hopper.abdera.response.StaticFeedResponseHandler;
import net.jps.atom.hopper.abdera.response.ResponseHandler;
import net.jps.atom.hopper.abdera.response.StaticEmptyBodyResponseHandler;
import net.jps.atom.hopper.abdera.response.StaticEntryResponseHandler;
import net.jps.atom.hopper.adapter.FeedPublisher;
import net.jps.atom.hopper.adapter.FeedSource;
import org.apache.abdera.protocol.server.TargetType;

import net.jps.atom.hopper.response.EmptyBody;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;

import net.jps.atom.hopper.config.v1_0.FeedConfiguration;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

public class FeedAdapter extends TargetAwareAbstractCollectionAdapter {

    private static final Logger LOG = new RCLogger(FeedAdapter.class);
    private final FeedConfiguration feedConfiguration;
    
    private final FeedPublisher feedPublisher;
    private final FeedSource feedSource;
    
    //TODO: Recompose?
    private final ResponseHandler<Feed> feedResponseHandler;
    private final ResponseHandler<EmptyBody> emptyBodyResponseHandler;
    private final ResponseHandler<Entry> entryResponseHandler;
    
    private String author;

    public FeedAdapter(FeedConfiguration feedConfiguration, FeedSource feedSourequeste) {
        //TODO: Replace null with a basic publisher that returns some 4xx status code
        this(feedConfiguration, feedSourequeste, null);
    }

    public FeedAdapter(FeedConfiguration feedConfiguration, FeedSource feedSourequeste, FeedPublisher feedPublisher) {
        this.feedConfiguration = feedConfiguration;
        this.feedSource = feedSourequeste;
        this.feedPublisher = feedPublisher;

        feedResponseHandler = new StaticFeedResponseHandler();
        entryResponseHandler = new StaticEntryResponseHandler();
        emptyBodyResponseHandler = new StaticEmptyBodyResponseHandler();

        author = "";
    }

    public FeedConfiguration getFeedConfiguration() {
        return feedConfiguration;
    }

    @Override
    public String getHref(RequestContext request) {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("collection", "feed");

        return request.urlFor(TargetType.TYPE_COLLECTION, params);
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public String getAuthor(RequestContext request) {
        return author;
    }

    @Override
    //TODO: Reimplement this - getting there
    public String getId(RequestContext request) {
//        return new StringBuilder("tag:").append(feedConfig.getFullUri()).append(",").append(CALENDAR_INSTANCE.get(Calendar.YEAR)).append(":").append(config.getBaseUrn()).toString();
        return "TODO: ID";
    }

    @Override
    public String getTitle(RequestContext request) {
        return getFeedConfiguration().getTitle();
    }

    @Override
    public ResponseContext getFeed(RequestContext request) {
        try {
            return feedResponseHandler.handleAdapterResponse(request, feedSource.getFeed(request));
        } catch (UnsupportedOperationException uoe) {
            return ProviderHelper.notallowed(request, uoe.getMessage(), new String[0]); //TODO: Fix this var-args bullshit
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext postEntry(RequestContext request) {
        if (feedPublisher == null) {
            return ProviderHelper.notsupported(request);
        }

        try {
            final AdapterResponse<Entry> response = feedPublisher.postEntry(request);

            if (response.getResponseStatus() == HttpStatusCode.CREATED) {
                final Entry returnedEntryCopy = response.getBody();

                //TODO: Push into filter
                if (StringUtilities.isBlank(returnedEntryCopy.getId().toString())) {
                    LOG.warn("New ID for Entry Update was not returned. Please verify that your adapter sets the entry's ID field");
                }
            }

            return entryResponseHandler.handleAdapterResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext putEntry(RequestContext request) {
        if (feedPublisher == null) {
            return ProviderHelper.notsupported(request);
        }

        try {
            final AdapterResponse<Entry> response = feedPublisher.putEntry(request);

            return entryResponseHandler.handleAdapterResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext deleteEntry(RequestContext request) {
        if (feedPublisher == null) {
            return ProviderHelper.notsupported(request);
        }

        try {
            final AdapterResponse<EmptyBody> response = feedPublisher.deleteEntry(request);

            return emptyBodyResponseHandler.handleAdapterResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext getEntry(RequestContext request) {
        try {
            return entryResponseHandler.handleAdapterResponse(request, feedSource.getEntry(request));
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }
}
