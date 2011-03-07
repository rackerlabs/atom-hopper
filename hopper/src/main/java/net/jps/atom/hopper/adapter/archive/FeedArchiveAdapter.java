package net.jps.atom.hopper.adapter.archive;

import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.response.AdapterResponse;
import java.util.Calendar;
import net.jps.atom.hopper.adapter.AdapterToolsAware;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 * This interface describes an adapter that is able to archive and store feeds as
 * well as retrieve them. This interface may be tagged onto a FeedSourceAdapter
 * to combine the Functionality of both in one class.
 *
 * @author John Hopper
 */
public interface FeedArchiveAdapter extends AdapterToolsAware {

    /**
     * Sets the archive job interval.
     * 
     * @param archivalIntervalInMiliseconds
     */
    void setArchivalInterval(int archivalIntervalInMiliseconds);

    /**
     * Gets the archive job interval
     * 
     * @return
     */
    int getArchivalInterval();

    /**
     * Requests an archived version of the feed.
     * 
     * @param request
     * @param date
     * The date may be null if not explicitly specified by the request
     * 
     * @return
     */
    AdapterResponse<Feed> getArchivedFeed(RequestContext request, Calendar requestedDate);

    /**
     * Requests that an archival job using this archive adapter begin for the
     * given start time.
     * 
     * @param archiveTime
     * @throws ArchiveProcessingException 
     */
    void archiveFeed(FeedSourceAdapter adapter, Calendar archiveTime) throws ArchiveProcessingException;
}
