/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.impl;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class UnimplementedFeedSource implements FeedSource {

    @Override
    public AdapterResponse<Entry> getEntry(RequestContext request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Feed> getFeed(RequestContext request) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Feed getFeedByDateRange(Calendar startingEntryDate, Calendar lastEntryDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
