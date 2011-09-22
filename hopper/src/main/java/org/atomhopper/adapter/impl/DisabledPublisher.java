package org.atomhopper.adapter.impl;

import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;

public final class DisabledPublisher extends AbstractDisabledAdapter implements FeedPublisher {

    private static final DisabledPublisher INSTANCE = new DisabledPublisher();
    private static final String OP_NOT_SUPPORTED_MESSAGE = "Operation not supported";

    public static DisabledPublisher getInstance() {
        return INSTANCE;
    }

    private DisabledPublisher() {
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest) {
        return ResponseBuilder.notImplemented(OP_NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        return ResponseBuilder.notImplemented(OP_NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest) {
        return ResponseBuilder.notImplemented(OP_NOT_SUPPORTED_MESSAGE);
    }
}
