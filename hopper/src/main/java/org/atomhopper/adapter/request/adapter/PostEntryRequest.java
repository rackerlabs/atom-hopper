package org.atomhopper.adapter.request.adapter;

import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.request.feed.FeedRequest;

public interface PostEntryRequest extends FeedRequest {

    Entry getEntry();
}
