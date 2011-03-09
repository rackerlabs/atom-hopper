package net.jps.atom.hopper.adapter.archive;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.FeedSource;

/**
 * A feed archiver is responsible for handling the actual archival process of a
 * lossy feed. The archiver should generate static variants of a live feed but
 * is not expected to explicitly link the generated archive to others. The ATOM
 * server will attempt to perform this operation automatically.
 * 
 * Note: this interface is required to schedule custom archival jobs
 */
public interface FeedArchiver {

    /**
     * Sets the archive job interval.
     * 
     * TODO: Build a cron-job style spec for this
     * 
     * @param archivalIntervalInMiliseconds
     */
    void setArchivalIntervalSpec(int archivalIntervalInMiliseconds);

    /**
     * Gets the archive job interval.
     * 
     * TODO: Build a cron-job style spec for this
     * 
     * @return
     */
    int archivalIntervalSpec();
    
    /**
     * Requests that an archival job using this archive adapter begin for the
     * given start time.
     * 
     * @param adapter
     * @param archiveTime
     * @throws ArchiveProcessingException 
     */
    void archiveFeed(FeedSource adapter, Calendar archiveTime) throws ArchiveProcessingException;
}
