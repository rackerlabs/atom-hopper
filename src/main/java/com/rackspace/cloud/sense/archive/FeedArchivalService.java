/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.archive;

import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;

/**
 *
 * @author zinic
 */
public interface FeedArchivalService {

    void startService();

    void stopService();

    void registerArchiver(FeedArchiver archiver);
}
