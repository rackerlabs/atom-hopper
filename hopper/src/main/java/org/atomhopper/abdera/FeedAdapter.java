package org.atomhopper.abdera;

import org.atomhopper.abdera.response.ResponseHandler;
import org.atomhopper.abdera.response.StaticEmptyBodyResponseHandler;
import org.atomhopper.abdera.response.StaticEntryResponseHandler;
import org.atomhopper.abdera.response.StaticFeedResponseHandler;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.atomhopper.adapter.request.adapter.impl.DeleteEntryRequestImpl;
import org.atomhopper.adapter.request.adapter.impl.GetCategoriesRequestImpl;
import org.atomhopper.adapter.request.adapter.impl.GetEntryRequestImpl;
import org.atomhopper.adapter.request.adapter.impl.GetFeedRequestImpl;
import org.atomhopper.adapter.request.adapter.impl.PostEntryRequestImpl;
import org.atomhopper.adapter.request.adapter.impl.PutEntryRequestImpl;

public class FeedAdapter extends TargetAwareAbstractCollectionAdapter {

//    private static final Logger LOG = new RCLogger(FeedAdapter.class);

    private static final String[] FEED_SOURCE_METHODS = {"GET"},
//            FEED_PUBLISHER_METHODS = {"POST", "PUT", "DELETE"},
            COMBINED_ALLOWED_METHODS = {"GET", "POST", "PUT", "DELETE"};

    private final String[] allowedMethods;
    private final FeedConfiguration feedConfiguration;
    private final FeedPublisher feedPublisher;
    private final FeedSource feedSource;

    //TODO: Recompose?
    private final ResponseHandler<Feed> feedResponseHandler;
    private final ResponseHandler<EmptyBody> emptyBodyResponseHandler;
    private final ResponseHandler<Entry> entryResponseHandler;
    private String author;

    public FeedAdapter(String target, FeedConfiguration feedConfiguration, FeedSource feedSource, FeedPublisher feedPublisher) {
        super(target);

        this.feedConfiguration = feedConfiguration;
        this.feedSource = feedSource;
        this.feedPublisher = feedPublisher;

        //TODO: Replace null with a basic publisher that returns some 4xx status code
        if (this.feedPublisher == null) {
            allowedMethods = FEED_SOURCE_METHODS;
        } else {
            allowedMethods = COMBINED_ALLOWED_METHODS;
        }

        feedResponseHandler = new StaticFeedResponseHandler(feedConfiguration);
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
        params.put("collection", getTarget());

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
    public ResponseContext getCategories(RequestContext request) {
        try {
            return ProviderHelper.returnBase(feedSource.getCategories(
                    new GetCategoriesRequestImpl(request)).getBody(),
                    HttpStatus.OK.value(), Calendar.getInstance().getTime());
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

//  TODO: Implement this?
//
//    @Override
//    public CategoriesInfo[] getCategoriesInfo(RequestContext request) {
//        return super.getCategoriesInfo(request);
//    }

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
            return feedResponseHandler.handleAdapterResponse(request, feedSource.getFeed(new GetFeedRequestImpl(request)));
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    //TODO: Migrate publishing work to a child class maybe... meh
    @Override
    public ResponseContext postEntry(RequestContext request) {
        if (feedPublisher == null) {
            return ProviderHelper.notallowed(request, allowedMethods);
        }

        try {
            final AdapterResponse<Entry> response = feedPublisher.postEntry(new PostEntryRequestImpl(request));

            return entryResponseHandler.handleAdapterResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext putEntry(RequestContext request) {
        if (feedPublisher == null) {
            return ProviderHelper.notallowed(request, allowedMethods);
        }

        try {
            final AdapterResponse<Entry> response = feedPublisher.putEntry(new PutEntryRequestImpl(request));

            return entryResponseHandler.handleAdapterResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext deleteEntry(RequestContext request) {
        if (feedPublisher == null) {
            return ProviderHelper.notallowed(request, allowedMethods);
        }

        try {
            final AdapterResponse<EmptyBody> response = feedPublisher.deleteEntry(new DeleteEntryRequestImpl(request));

            return emptyBodyResponseHandler.handleAdapterResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext getEntry(RequestContext request) {
        try {
            return entryResponseHandler.handleAdapterResponse(request, feedSource.getEntry(new GetEntryRequestImpl(request)));
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }
}
