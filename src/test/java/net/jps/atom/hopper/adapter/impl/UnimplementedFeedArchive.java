/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.jps.atom.hopper.adapter.impl;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.AdapterTools;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.adapter.archive.ArchiveProcessingException;
import net.jps.atom.hopper.adapter.archive.FeedArchiveAdapter;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class UnimplementedFeedArchive implements FeedArchiveAdapter {

    @Override
    public void setAdapterTools(AdapterTools tools) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void archiveFeed(FeedSourceAdapter adapter, Calendar archiveTime) throws ArchiveProcessingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getArchivalInterval() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Feed> getArchivedFeed(RequestContext request, Calendar requestedDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setArchivalInterval(int archivalIntervalInMiliseconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
