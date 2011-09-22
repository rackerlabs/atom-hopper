package org.atomhopper.adapter.impl;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;

public final class DisabledFeedSource extends AbstractDisabledAdapter implements FeedSource {

    private static final DisabledFeedSource INSTANCE = new DisabledFeedSource();
    private static final String OP_NOT_SUPPORTED_MESSAGE = "Operation not supported";

    public static DisabledFeedSource getInstance() {
        return INSTANCE;
    }
    
    private DisabledFeedSource() {
    }
    
    @Override
    public FeedInformation getFeedInformation() {
        return DisabledFeedInformation.getInstance();
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        return ResponseBuilder.notImplemented(OP_NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        return ResponseBuilder.notImplemented(OP_NOT_SUPPORTED_MESSAGE);
    }
}
