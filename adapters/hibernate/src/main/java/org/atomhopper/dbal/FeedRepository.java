package org.atomhopper.dbal;

import java.util.Collection;
import java.util.List;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.jpa.PersistedEntry;

public interface FeedRepository {

    Collection<PersistedFeed> getAllFeeds();

    PersistedFeed getFeed(String resourceName);
    
    void saveFeed(PersistedFeed feed);

    List<PersistedEntry> getFeedHead(String feedName, int pageSize);
    
    List<PersistedEntry> getFeedPage(String feedName, PersistedEntry markerEntry, int pageSize, PageDirection direction);
    
    PersistedEntry getEntry(String entryId);

    void saveEntry(PersistedEntry entry);
}
