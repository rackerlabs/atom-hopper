/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.archive;

import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;
import java.util.Calendar;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public class FeedArchivalRequest {

    private final Feed feedBeingArchived;
    private final Calendar archivalTime;
    private final FeedArchiver archiver;
    private final FeedArchivalFuture archivalFuture;

    public FeedArchivalRequest(FeedArchiver archiver, Feed feedBeingArchived, Calendar archivalTime) {
        this.feedBeingArchived = feedBeingArchived;
        this.archivalTime = archivalTime;
        this.archiver = archiver;
        
        archivalFuture = new FeedArchivalFutureImpl();
    }

    public FeedArchiver getArchiver() {
        return archiver;
    }

    public Calendar getArchivalTime() {
        return archivalTime;
    }

    public Feed getFeedBeingArchived() {
        return feedBeingArchived;
    }

    public FeedArchivalFuture getArchivalFuture() {
        return archivalFuture;
    }
}
