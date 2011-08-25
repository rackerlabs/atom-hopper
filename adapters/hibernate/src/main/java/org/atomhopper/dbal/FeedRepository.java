package org.atomhopper.dbal;

import java.util.Collection;
import org.atomhopper.adapter.jpa.Feed;
import org.atomhopper.adapter.jpa.FeedEntry;

public interface FeedRepository {

    Collection<Feed> getAllFeeds();

    Feed getFeed(String resourceName);
    
    void saveFeed(String feedName);

    FeedEntry getEntry(String entryId);

    void saveEntry(FeedEntry entry);
}
