/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.archive;

import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.adapter.archive.FeedArchiver;

/**
 *
 * @author zinic
 */
public interface FeedArchivalService {

    void startService();

    void stopService();

    void registerArchiveTask(FeedSource feedSource, FeedArchiver archiver);
}
