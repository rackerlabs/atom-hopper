/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.archive.impl;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import net.jps.atom.hopper.archive.FeedArchivalService;
import net.jps.atom.hopper.adapter.archive.FeedArchiveAdapter;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zinic
 */
public class QueuedFeedArchivalService extends TimerTask implements FeedArchivalService {

    private static final Logger LOG = new RCLogger(QueuedFeedArchivalService.class);

    //In milliseconds
    public static final long QUEUE_SCAN_INTERVAL = 60000;
    public static final int MAX_THREADS = Runtime.getRuntime().availableProcessors() + 2;

    private final List<FeedArchivalTask> feedArchivalTasks;
    private final ExecutorService archivalThreadPool;
    private final Timer queueScanTimer;

    public QueuedFeedArchivalService() {
        queueScanTimer = new Timer("QueuedFeedArchivalService::QueueScanTimer", true);
        feedArchivalTasks = new LinkedList<FeedArchivalTask>();

        archivalThreadPool = Executors.newFixedThreadPool(MAX_THREADS);
    }

    @Override
    public synchronized void run() {
        for (FeedArchivalTask archivalTask : feedArchivalTasks) {
            if (archivalTask.shouldArchive()) {
                archivalThreadPool.submit(archivalTask);
            }
        }
    }

    @Override
    public synchronized void registerArchiver(FeedArchiveAdapter archiver) {
        feedArchivalTasks.add(new FeedArchivalTask(archiver));
    }

    @Override
    public synchronized void startService() {
        if (archivalThreadPool.isShutdown() || archivalThreadPool.isTerminated()) {
            throw new IllegalStateException("Archival service already shutdown - can not restart!");
        }

        queueScanTimer.schedule(this, System.currentTimeMillis(), QUEUE_SCAN_INTERVAL);
    }

    @Override
    public synchronized void stopService() {
        queueScanTimer.purge();

        archivalThreadPool.shutdown(); // Disable new tasks from being submitted

        try {
            // Wait a while for existing tasks to terminate
            if (!archivalThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                archivalThreadPool.shutdownNow(); // Cancel currently executing tasks

                // Wait a while for tasks to respond to being cancelled
                if (!archivalThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOG.error("Archival thread pool unable to terminate. This is a bug.");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            archivalThreadPool.shutdownNow();

            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
