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
public interface FeedArchivalService {

    void stopService();

    void queueFeedArchival(FeedArchiver archiver, Feed f, Calendar cal);
}
