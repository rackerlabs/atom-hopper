/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.archive.manager;

import com.rackspace.cloud.commons.util.thread.TimerThread;
import com.rackspace.cloud.sense.archive.QueuingFeedArchivalService;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public class FeedArchivalManager implements Runnable {

    private final List<FeedArchivalTask> archivalTasks;
    private final QueuingFeedArchivalService archivalService;
    private final TimerThread timeKeeper;

    public FeedArchivalManager(QueuingFeedArchivalService archivalService) {
        this.archivalService = archivalService;

        timeKeeper = new TimerThread(this);
        archivalTasks = new LinkedList<FeedArchivalTask>();
    }

    public void addArchivalTask(FeedArchivalTask task) {
        archivalTasks.add(task);
    }

    @Override
    public void run() {
        for (FeedArchivalTask task : archivalTasks) {
            if (task.shouldArchive()) {
                //Previous archival period represents the time of the last entry retrieved from feed
                final Calendar archivalTime = task.getArchivalTime();
                final AdapterResponse<Feed> f = task.feedAdapter().getFeed(archivalTime);

                //Flip to the next archival time period - i.e. now
                task.turnArchivalTime();

                //Queue archival with the feed and the new archival time - i.e. now
                archivalService.queueFeedArchival(task.archiver(), f.getBody(), task.getArchivalTime());
            }
        }
    }

    public void start() {
        timeKeeper.start();
    }

    public void setArchivalInterval(long intervalInMilliseconds) {
        timeKeeper.setWaitInterval(intervalInMilliseconds);
    }
}
