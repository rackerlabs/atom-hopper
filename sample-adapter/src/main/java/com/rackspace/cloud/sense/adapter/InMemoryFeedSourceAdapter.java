/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.adapter;

import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import net.jps.atom.hopper.adapter.StoredEntry;
import net.jps.atom.hopper.adapter.AdapterTools;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.adapter.ResponseBuilder;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class InMemoryFeedSourceAdapter implements FeedSourceAdapter {

    private final SortedSet<StoredEntry> storedEntrySet;
    private AdapterTools adapterToolsReference;

    public InMemoryFeedSourceAdapter() {
        storedEntrySet = new TreeSet<StoredEntry>();
    }

    @Override
    public void setAdapterTools(AdapterTools at) {
        adapterToolsReference = at;
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(RequestContext rc, String id) throws UnsupportedOperationException {
        StoredEntry removeMe = null;

        for (StoredEntry se : storedEntrySet) {
            final Entry e = se.getStoredEntry();

            if (e.getId().toString().equals(id)) {
                removeMe = se;
                break;
            }
        }

        if (removeMe != null) {
            storedEntrySet.remove(removeMe);
            return ResponseBuilder.ok();
        }

        return ResponseBuilder.notFound();
    }

    @Override
    public AdapterResponse<Entry> getEntry(RequestContext rc, String id) throws UnsupportedOperationException {
        for (StoredEntry se : storedEntrySet) {
            final Entry e = se.getStoredEntry();

            if (e.getId().toString().equals(id)) {
                return ResponseBuilder.found(e);
            }
        }

        return ResponseBuilder.notFound();
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext rc) {
        final Feed newFeed = adapterToolsReference.newFeed();

        //TODO: Find a more consistent way of adding an ID to the feed?
        newFeed.setId("http://localhost:8080/service/events/");
        newFeed.setTitle("Some Feed");
        newFeed.addAuthor("Rackspace Cloud");

        int count = 0;

        for (StoredEntry se : storedEntrySet) {
            if (count >= 10) {
                break;
            }

            newFeed.addEntry(se.getStoredEntry());
            count++;
        }

        return ResponseBuilder.found(newFeed);
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request, String markerId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Feed getFeedByDateRange(Calendar clndr, Calendar clndr1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> postEntry(RequestContext rc, Entry entry) throws UnsupportedOperationException {
        String id;

        try {
            id = "urn:uuid:" + UUID.randomUUID().toString();
        } catch (Exception ex) {
            //Log this
            return ResponseBuilder.error("Internal error");
        }

        entry.setId(id);
        storedEntrySet.add(new StoredEntry(entry));

        return ResponseBuilder.created(entry);

    }

    @Override
    public AdapterResponse<Entry> putEntry(RequestContext rc, String string, Entry entry) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
