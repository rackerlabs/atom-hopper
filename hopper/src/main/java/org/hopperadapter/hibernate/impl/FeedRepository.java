package org.hopperadapter.hibernate.impl;

import java.util.Collection;
import org.atomhopper.adapter.jpa.Feed;
import org.atomhopper.adapter.jpa.FeedEntry;

public interface FeedRepository {

    Collection<Feed> getAllFeeds();

    Feed getFeed(String resourceName);
    
    void addFeed(String feedName);

    FeedEntry getEntry(String entryId);

    void addEntry(FeedEntry entry);
}
