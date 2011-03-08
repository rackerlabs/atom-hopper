/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.archive;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.FeedSource;

/**
 *
 * @author zinic
 */
public interface FeedArchiver {

    /**
     * Sets the archive job interval.
     * 
     * @param archivalIntervalInMiliseconds
     */
    void setArchivalIntervalSpec(int archivalIntervalInMiliseconds);

    /**
     * Gets the archive job interval
     * 
     * @return
     */
    int archivalIntervalSpec();

    /**
     * Requests that an archival job using this archive adapter begin for the
     * given start time.
     * 
     * @param archiveTime
     * @throws ArchiveProcessingException 
     */
    void archiveFeed(FeedSource adapter, Calendar archiveTime) throws ArchiveProcessingException;
}
