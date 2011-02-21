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
public class QueuingFeedArchivalService implements Runnable, FeedArchivalService {

    private static final Logger LOG = new RCLogger(QueuingFeedArchivalService.class);

    private final ExecutorService archivalExecutorPool;
    private final Queue<FeedArchivalRequest> archivalQueue;
    private final Lock archivalQueueLock;
    private final Condition queueNotEmpty;

    private boolean running;

    public QueuingFeedArchivalService(WorkspaceConfig config) {
        archivalExecutorPool = Executors.newFixedThreadPool((config.getFeed().size() / 5) + 2, Executors.defaultThreadFactory());

        archivalQueueLock = new ReentrantLock(true);
        queueNotEmpty = archivalQueueLock.newCondition();

        archivalQueue = new LinkedList<FeedArchivalRequest>();
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            try {
                archivalQueueLock.lock();

                while (running && archivalQueue.isEmpty()) {
                    queueNotEmpty.await();
                }
            } catch (InterruptedException ie) {
                LOG.info("FeedArchivalService shutting down. Reason: Interrupted.", ie);
                stopService();
            } finally {
                archivalQueueLock.unlock();
            }

            processNextArchive();
        }
    }

    private void processNextArchive() {
        if (running) {
            final FeedArchivalRequest nextArchive = archivalQueue.poll();

            LOG.info("Submitting feed for archival: " + nextArchive.getFeedBeingArchived().getTitle());
            archivalExecutorPool.submit(new ArchiveExecutor(nextArchive));
        }
    }

    @Override
    public void queueFeedArchival(FeedArchiver archiver, Feed f, Calendar cal) {
        final FeedArchivalRequest archive = new FeedArchivalRequest(archiver, f, cal);

        try {
            archivalQueueLock.lock();
            archivalQueue.add(archive);

            queueNotEmpty.signal();
        } finally {
            archivalQueueLock.unlock();
        }
    }

    @Override
    public synchronized void stopService() {
        running = false;
    }

    private class ArchiveExecutor implements Runnable {

        private final FeedArchivalRequest archivalRequest;

        public ArchiveExecutor(FeedArchivalRequest archivalRequest) {
            this.archivalRequest = archivalRequest;
        }

        @Override
        public void run() {
            archivalRequest.getArchiver().archiveFeed(archivalRequest.getFeedBeingArchived(), archivalRequest.getArchivalTime());
        }
    }
}
