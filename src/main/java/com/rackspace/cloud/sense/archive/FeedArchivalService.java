/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.archive;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;
import com.rackspace.cloud.sense.config.v1_0.WorkspaceConfig;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public class FeedArchivalService implements Runnable {

    private final Logger log = new RCLogger(FeedArchivalService.class);
//    private final List<SenseFeedAdapter> registeredArchivableAdapters;
    private final ExecutorService executorPool;
    private final Queue<FeedArchivalRequest> archivalQueue;
    private final Lock archivalQueueLock;
    private final Condition queueNotEmpty;
    private volatile boolean running;

    public FeedArchivalService(WorkspaceConfig config) {
        executorPool = Executors.newFixedThreadPool((config.getFeed().size() / 5) + 2, Executors.defaultThreadFactory());

        archivalQueueLock = new ReentrantLock(true);
        queueNotEmpty = archivalQueueLock.newCondition();
        
        archivalQueue = new LinkedList<FeedArchivalRequest>();
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            FeedArchivalRequest nextArchive = null;

            try {
                archivalQueueLock.lock();

                while (running && archivalQueue.isEmpty()) {
                    queueNotEmpty.await();
                }

                nextArchive = archivalQueue.poll();
            } catch (InterruptedException ie) {
                log.info("FeedArchivalService shutting down. Reason: Interrupted.", ie);
                break;
            } finally {
                archivalQueueLock.unlock();
            }

            if (running) {
                nextArchive.getArchiver().archiveFeed(nextArchive.getFeedBeingArchived(), nextArchive.getArchivalTime());
            }
        }
    }

    public void process() {
        FeedArchivalRequest nextArchive = null;

        try {
            archivalQueueLock.lock();
            nextArchive = archivalQueue.poll();
        } finally {
            archivalQueueLock.unlock();
        }

        if (running && nextArchive != null) {
            executorPool.submit(new ArchiveExecutor(nextArchive));
        }
    }

    private class ArchiveExecutor implements Runnable {

        private final FeedArchivalRequest archivalRequest;

        public ArchiveExecutor(FeedArchivalRequest archivalRequest) {
            this.archivalRequest = archivalRequest;
        }

        public void run() {
            archivalRequest.getArchiver().archiveFeed(archivalRequest.getFeedBeingArchived(), archivalRequest.getArchivalTime());
        }
    }

    public void dispose() {
        running = false;
        //TODO: Implement
    }

    public FeedArchivalFuture queueFeedArchival(FeedArchiver archiver, Feed f, Date d) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        final FeedArchivalRequest archive = new FeedArchivalRequest(archiver, f, cal);

        try {
            archivalQueueLock.lock();
            archivalQueue.add(archive);

            queueNotEmpty.signal();
        } finally {
            archivalQueueLock.unlock();
        }

        return archive.getArchivalFuture();
    }
}
