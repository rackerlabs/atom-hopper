/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.archive.impl;

import net.jps.atom.hopper.client.adapter.archive.FeedArchiver;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author zinic
 */
public class FeedArchivalTask implements Runnable {

    public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getDefault();
    
    private final FeedArchiver archiver;
    private final TimeZone timeZone;
    
    private long lastArchivalStamp;

    public FeedArchivalTask(FeedArchiver archiver) {
        this(archiver, DEFAULT_TIME_ZONE);
    }

    public FeedArchivalTask(FeedArchiver archiver, TimeZone timeZone) {
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
        archiver.archiveFeed(archivalTime());
    }

    public boolean shouldArchive() {
        final long now = now();
        final boolean shouldArchive = now - lastArchivalStamp > archiver.getArchivalInterval();

        if (shouldArchive) {
            lastArchivalStamp = now;
        }

        return shouldArchive;
    }
}
