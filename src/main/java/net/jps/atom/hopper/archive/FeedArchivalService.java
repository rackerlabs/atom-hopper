/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.archive;

import net.jps.atom.hopper.adapter.archive.FeedArchiveAdapter;

/**
 *
 * @author zinic
 */
public interface FeedArchivalService {

    void startService();

    void stopService();

    void registerArchiver(FeedArchiveAdapter archiver);
}
