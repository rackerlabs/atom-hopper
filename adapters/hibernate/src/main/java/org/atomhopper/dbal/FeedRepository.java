package org.atomhopper.dbal;

import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.hibernate.query.CategoryCriteriaGenerator;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface FeedRepository {

    Set<PersistedCategory> getCategoriesForFeed(final String feedName);

    Collection<PersistedFeed> getAllFeeds();

    PersistedFeed getFeed(String resourceName);

    void saveFeed(PersistedFeed feed);

    List<PersistedEntry> getFeedHead(String feedName, CategoryCriteriaGenerator categoryCriteria, int pageSize);

    List<PersistedEntry> getFeedPage(String feedName, PersistedEntry markerEntry, PageDirection direction,
            CategoryCriteriaGenerator categoryCriteria, int pageSize);

    PersistedEntry getEntry(String entryId, String feedName);

    List<PersistedEntry> getLastPage(String feedName, int pageSize, CategoryCriteriaGenerator criteriaGenerator);

    void saveEntry(PersistedEntry entry);

    Set<PersistedCategory> updateCategories(Set<PersistedCategory> categories);

    PersistedEntry getNextMarker(PersistedEntry persistedEntry, String feedName, CategoryCriteriaGenerator criteriaGenerator);

    Integer getFeedCount(String feedName, CategoryCriteriaGenerator criteriaGenerator);
}
