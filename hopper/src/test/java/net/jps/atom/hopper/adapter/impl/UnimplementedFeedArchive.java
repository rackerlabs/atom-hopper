/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.impl;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.archive.FeedArchiveSource;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class UnimplementedFeedArchive implements FeedArchiveSource {

    @Override
    public AdapterResponse<Feed> getArchivedFeed(RequestContext request, Calendar requestedDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
