package org.atomhopper.adapter.request.adapter;

import org.atomhopper.adapter.request.feed.FeedRequest;
import org.apache.abdera.model.Entry;

public interface PostEntryRequest extends FeedRequest {

    Entry getEntry();
}
