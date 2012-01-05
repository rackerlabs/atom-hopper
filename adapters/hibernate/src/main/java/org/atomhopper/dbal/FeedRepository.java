package org.atomhopper.dbal;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.hibernate.query.CategoryCriteriaGenerator;

public interface FeedRepository {

    Set<PersistedCategory> getCategoriesForFeeed(final String feedName);
    
    Collection<PersistedFeed> getAllFeeds();

    PersistedFeed getFeed(String resourceName);
    
    void saveFeed(PersistedFeed feed);

    List<PersistedEntry> getFeedHead(String feedName, CategoryCriteriaGenerator categoryCriteria, int pageSize);
    
    List<PersistedEntry> getFeedPage(String feedName, PersistedEntry markerEntry, PageDirection direction, CategoryCriteriaGenerator categoryCriteria, int pageSize);
    
    PersistedEntry getEntry(String entryId);

    void saveEntry(PersistedEntry entry);
    
    Set<PersistedCategory> updateCategories(Set<PersistedCategory> categories);
}
