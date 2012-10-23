package org.atomhopper.abdera;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.server.ProviderHelper;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.TargetType;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.abdera.filter.FeedEntityTagProcessor;
import org.atomhopper.abdera.filter.FeedPagingProcessor;
import org.atomhopper.abdera.response.EmptyBodyResponseHandler;
import org.atomhopper.abdera.response.EntryResponseHandler;
import org.atomhopper.abdera.response.FeedResponseHandler;
import org.atomhopper.abdera.response.ResponseHandler;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.impl.DisabledFeedSource;
import org.atomhopper.adapter.impl.DisabledPublisher;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.adapter.request.adapter.impl.*;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.springframework.http.HttpStatus;

import java.util.*;

public class FeedAdapter extends TargetAwareAbstractCollectionAdapter {

    private final ResponseHandler<EmptyBody> emptyBodyResponseHandler;
    private final ResponseHandler<Feed> feedResponseHandler;
    private final ResponseHandler<Entry> entryResponseHandler;
    private final FeedConfiguration feedConfiguration;
    private final FeedPublisher feedPublisher;
    private final FeedSource feedSource;

    public FeedAdapter(String target, FeedConfiguration feedConfiguration, FeedSource feedSource, FeedPublisher feedPublisher) {
        super(target);

        this.feedConfiguration = feedConfiguration;

        final List<String> allowedMethodsList = new LinkedList<String>();

        if (feedSource != null) {
            this.feedSource = feedSource;

            allowedMethodsList.add("GET");
        } else {
            this.feedSource = DisabledFeedSource.getInstance();
        }

        if (feedPublisher != null) {
            this.feedPublisher = feedPublisher;

            allowedMethodsList.add("PUT");
            allowedMethodsList.add("POST");
            allowedMethodsList.add("DELETE");
        } else {
            this.feedPublisher = DisabledPublisher.getInstance();
        }

        final String[] allowedMethods = allowedMethodsList.toArray(new String[allowedMethodsList.size()]);

        feedResponseHandler = new FeedResponseHandler(allowedMethods, new FeedPagingProcessor(), new FeedEntityTagProcessor());
        entryResponseHandler = new EntryResponseHandler(allowedMethods);
        emptyBodyResponseHandler = new EmptyBodyResponseHandler(allowedMethods);
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

    @Override
    public String getAuthor(RequestContext request) {
        return getFeedConfiguration().getAuthor().getName();
    }

    @Override
    public ResponseContext getCategories(RequestContext request) {
        final GetCategoriesRequest getCategoriesRequest = new GetCategoriesRequestImpl(request);

        try {
            return ProviderHelper
                    .returnBase(feedSource.getFeedInformation().getCategories(getCategoriesRequest), HttpStatus.OK.value(), Calendar.getInstance().getTime());
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
    public String getId(RequestContext request) {
        return feedSource.getFeedInformation().getId(new GetFeedRequestImpl(request));
    }

    @Override
    public String getTitle(RequestContext request) {
        return getFeedConfiguration().getTitle();
    }

    @Override
    public ResponseContext getFeed(RequestContext request) {
        GetFeedRequest getFeedRequest = new GetFeedRequestImpl(request);
        final String limitErrorMessage = "Limit parameter not valid, acceptable values are 1 to 1000";
        final int minLimit = 1;
        final int maxLimit = 1000;

        try {
            final String pageSizeString = getFeedRequest.getPageSize();

            if (StringUtils.isNotBlank(pageSizeString)) {
                if ((Integer.parseInt(pageSizeString) < minLimit) || (Integer.parseInt(pageSizeString) > maxLimit)) {
                    return ProviderHelper.badrequest(request, limitErrorMessage);
                }
            }
        } catch (NumberFormatException nfe) {
            return ProviderHelper.badrequest(request, limitErrorMessage);
        }

        try {
            return feedResponseHandler.handleResponse(request, feedSource.getFeed(getFeedRequest));
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext postEntry(RequestContext request) {
        try {
            final AdapterResponse<Entry> response = feedPublisher.postEntry(new PostEntryRequestImpl(request));
            return entryResponseHandler.handleResponse(request, response);
        } catch (ParseException ex) {
            return ProviderHelper.createErrorResponse(Abdera.getInstance(), HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage(), ex);
        } catch (RequestParsingException rpex) {
            return ProviderHelper
                    .createErrorResponse(Abdera.getInstance(), HttpStatus.UNPROCESSABLE_ENTITY.value(), "The POST did not contain valid ATOM XML", rpex);
        } catch (IllegalArgumentException iae) {
            return ProviderHelper.badrequest(request, iae.getMessage());
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext putEntry(RequestContext request) {
        try {
            final AdapterResponse<Entry> response = feedPublisher.putEntry(new PutEntryRequestImpl(request));

            return entryResponseHandler.handleResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext deleteEntry(RequestContext request) {
        try {
            final AdapterResponse<EmptyBody> response = feedPublisher.deleteEntry(new DeleteEntryRequestImpl(request));

            return emptyBodyResponseHandler.handleResponse(request, response);
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }

    @Override
    public ResponseContext getEntry(RequestContext request) {
        try {
            return entryResponseHandler.handleResponse(request, feedSource.getEntry(new GetEntryRequestImpl(request)));
        } catch (Exception ex) {
            return ProviderHelper.servererror(request, ex.getMessage(), ex);
        }
    }
}
