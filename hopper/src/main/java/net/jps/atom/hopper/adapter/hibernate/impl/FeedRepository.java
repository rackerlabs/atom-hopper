package net.jps.atom.hopper.adapter.hibernate.impl;

import java.util.Collection;
import net.jps.atom.hopper.adapter.hibernate.impl.domain.Feed;
import net.jps.atom.hopper.adapter.hibernate.impl.domain.FeedEntry;

public interface FeedRepository {

    Collection<Feed> getAllFeeds();

    Feed getFeed(String resourceName);
    
    void addFeed(String feedName);

    FeedEntry getEntry(String entryId);

    void addEntry(FeedEntry entry);
}
