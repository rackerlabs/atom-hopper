/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.abdera;

import com.rackspace.cloud.sense.client.adapter.archive.impl.EntryInfo;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public class FeedChangeTracker {
    private final List<EntryInfo> entryList;

    public FeedChangeTracker() {
        entryList = new LinkedList<EntryInfo>();
    }

    public synchronized void putEntry(String entryId, Entry e) {
        entryList.add(new EntryInfo(entryId, e));
    }

    public synchronized void removeEntry(String entryId) {
        entryList.remove(new EntryInfo(entryId));
    }

    public synchronized List<EntryInfo> cloneEntryList() {
        final List<EntryInfo> clonedList = new LinkedList<EntryInfo>(entryList);
        entryList.clear();

        return clonedList;
    }

    public Feed populateFeed(Feed feed) {
        for (EntryInfo ei : cloneEntryList()) {
            feed.addEntry(ei.getEntry());
        }

        return feed;
    }
}
