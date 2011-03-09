package net.jps.atom.hopper.adapter.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import net.jps.atom.hopper.adapter.FeedPublisher;
import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.adapter.ResponseBuilder;
import net.jps.atom.hopper.adapter.request.DeleteEntryRequest;
import net.jps.atom.hopper.adapter.request.GetEntryRequest;
import net.jps.atom.hopper.adapter.request.GetFeedRequest;
import net.jps.atom.hopper.adapter.request.PostEntryRequest;
import net.jps.atom.hopper.adapter.request.PutEntryRequest;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *

 */
public class InMemoryFeedAdapter implements FeedSource, FeedPublisher {

    private final SortedMap<String, AtomEntry> liveFeed;

    public InMemoryFeedAdapter() {
        liveFeed = Collections.synchronizedSortedMap(new TreeMap<String, AtomEntry>());
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        final AtomEntry entry = liveFeed.get(getEntryRequest.getId());
        
        if (entry != null) {
            return ResponseBuilder.found(entry.getEntry());
        }
        
        return ResponseBuilder.notFound();
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        final Feed feed = getFeedRequest.getRequestContext().getAbdera().newFeed();
        
        feed.setTitle("A Feed");
        
        for (AtomEntry ae : liveFeed.values()) {
            feed.addEntry(ae.getEntry());
        }
        
        return ResponseBuilder.found(feed);
    }

    @Override
    public Feed getFeedByDateRange(Calendar startingEntryDate, Calendar lastEntryDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry entryToPost = postEntryRequest.getEntry();
        
        return ResponseBuilder.notFound();
    }

    @Override
    public AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
