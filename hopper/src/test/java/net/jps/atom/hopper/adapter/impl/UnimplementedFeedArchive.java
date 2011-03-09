/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.impl;

import net.jps.atom.hopper.adapter.archive.FeedArchiveSource;
import net.jps.atom.hopper.adapter.request.GetFeedArchiveRequest;
import net.jps.atom.hopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;

/**
 *
 * 
 */
public class UnimplementedFeedArchive implements FeedArchiveSource {

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedArchiveRequest getFeedArchiveRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
