package org.atomhopper.adapter.request.adapter;

import java.util.List;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.request.feed.FeedRequest;

/**
 *
 *
 */
public interface GetFeedRequest extends FeedRequest {

    Feed newFeed();

    List<String> getCategories();

    String getPageMarker();
    
    String getPageSize();
}
