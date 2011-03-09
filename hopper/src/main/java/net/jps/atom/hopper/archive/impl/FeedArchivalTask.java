package net.jps.atom.hopper.archive.impl;

import java.util.Calendar;
import java.util.TimeZone;
import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.adapter.archive.FeedArchiver;

/**
 *
 * 
 */
public class FeedArchivalTask implements Runnable {

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
    
    private final FeedSource feedSource;
    private final FeedArchiver archiver;
    private final TimeZone timeZone;
    private long lastArchivalStamp;

    public FeedArchivalTask(FeedSource feedSource, FeedArchiver archiver) {
       this(feedSource, archiver, DEFAULT_TIME_ZONE);
    }

    public FeedArchivalTask(FeedSource feedSource, FeedArchiver archiver, TimeZone timeZone) {
        this.feedSource = feedSource;
        this.archiver = archiver;
        this.timeZone = timeZone;

        lastArchivalStamp = now();
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private Calendar archivalTime() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZone);
        cal.setTimeInMillis(lastArchivalStamp);

        return cal;
    }

    @Override
    public void run() {
        archiver.archiveFeed(feedSource, archivalTime());
    }

    public boolean shouldArchive() {
        final long now = now();
        final boolean shouldArchive = now - lastArchivalStamp > archiver.archivalIntervalSpec();

        if (shouldArchive) {
            lastArchivalStamp = now;
        }

        return shouldArchive;
    }
}
