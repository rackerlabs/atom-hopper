/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jps.atom.hopper.adapter.impl;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.AdapterTools;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class UnimplementedFeedSource implements FeedSourceAdapter {

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(RequestContext request, String entryId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> getEntry(RequestContext request, String entryId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request, String markerId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Feed getFeedByDateRange(Calendar startingEntryDate, Calendar lastEntryDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> postEntry(RequestContext request, Entry entryToAdd) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> putEntry(RequestContext request, String entryId, Entry entryToUpdate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setAdapterTools(AdapterTools tools) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
