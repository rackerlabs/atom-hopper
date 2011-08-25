package org.atomhopper.adapter.request.entry;

import org.atomhopper.adapter.request.feed.FeedRequest;

public interface EntryRequest extends FeedRequest {

    String getEntryId();
}
