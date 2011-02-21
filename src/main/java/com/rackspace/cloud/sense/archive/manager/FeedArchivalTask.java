/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.archive.manager;

import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author zinic
 */
public class FeedArchivalTask {

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();

    private final TimeZone timeZone;
    private final FeedSourceAdapter feedAdapter;
    private final FeedArchiver archiver;
    private final long interval;
    
    private long lastIntervalCheck, lastArchivalTime;

    public FeedArchivalTask(FeedSourceAdapter feedAdapter, FeedArchiver archiver, long interval) {
        this(feedAdapter, archiver, interval, DEFAULT_TIME_ZONE);
    }

    public FeedArchivalTask(FeedSourceAdapter feedAdapter, FeedArchiver archiver, long interval, TimeZone timeZone) {
        this.timeZone = timeZone;
        this.feedAdapter = feedAdapter;
        this.archiver = archiver;
        this.interval = interval;

        //TODO: This needs to have state
        lastArchivalTime = -1;
        lastIntervalCheck = -1;
    }

    public FeedArchiver archiver() {
        return archiver;
    }

    public FeedSourceAdapter feedAdapter() {
        return feedAdapter;
    }

    public Calendar getArchivalTime() {
        final Calendar then = Calendar.getInstance();
        then.setTimeInMillis(lastArchivalTime);
        then.setTimeZone(timeZone);

        return then;
    }

    public void turnArchivalTime() {
        lastArchivalTime = now();
    }

    public boolean shouldArchive() {
        final long now = now();

        if (now - lastIntervalCheck > interval) {
            lastIntervalCheck = now;
        }

        return lastIntervalCheck == now;
    }

    private static long now() {
        return System.currentTimeMillis();
    }
}
