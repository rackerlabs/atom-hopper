package org.atomhopper.adapter.request.adapter;

import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.request.feed.FeedRequest;

import java.util.List;

/**
 *
 *
 */
public interface GetFeedRequest extends FeedRequest {

    Feed newFeed();

    List<String> getCategories();
    
    String getSearchQuery();

    String getPageMarker();
    
    String getPageSize();

    String getDirection();

    String getStartingAt();
}
