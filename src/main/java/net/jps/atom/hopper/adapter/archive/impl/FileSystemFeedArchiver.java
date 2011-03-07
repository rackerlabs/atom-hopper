/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.archive.impl;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.util.StringUtilities;
import net.jps.atom.hopper.adapter.archive.ArchiveProcessingException;
import net.jps.atom.hopper.adapter.archive.FeedArchiveAdapter;
import net.jps.atom.hopper.response.AdapterResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import net.jps.atom.hopper.adapter.AdapterTools;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.adapter.ResponseBuilder;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class FileSystemFeedArchiver implements FeedArchiveAdapter {

    private static final Logger log = new RCLogger(FileSystemFeedArchiver.class);

    public static final int HOUR_IN_MILLISECONDS = 3600000;
    public static final String ARCHIVE_FILE_EXTENSION = ".archive.xml";
    
    private final String archiveDirectoryRoot;
    
    private int archivalInterval;
    private FeedSourceAdapter feedAdapter;
    private AdapterTools adapterTools;

    public FileSystemFeedArchiver(String archiveDirectoryRoot) {
        this.archiveDirectoryRoot = archiveDirectoryRoot;
        archivalInterval = HOUR_IN_MILLISECONDS;
    }

    @Override
    public void init(AdapterTools tools, FeedSourceAdapter fsa) {
        adapterTools = tools;
        feedAdapter = fsa;
    }

    @Override
    public void archiveFeed(Calendar archivalTime) {
        final String destinationDirectory = StringUtilities.join(
                archiveDirectoryRoot,
                archiveDirectoryRoot.endsWith("/") ? "" : "/",
                archivalTime.get(Calendar.YEAR), "/",
                archivalTime.get(Calendar.MONTH), "/",
                archivalTime.get(Calendar.DAY_OF_MONTH));

        final String destinationFile = StringUtilities.join(
                destinationDirectory, "/",
                archivalTime.get(Calendar.HOUR_OF_DAY), ARCHIVE_FILE_EXTENSION);

        final File dir = new File(destinationDirectory);
        final File file = new File(destinationFile);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error("Unable to create archive directory, \"" + dir.getAbsolutePath() + "\"  --  Please check directory permissions");
            }
        } else if (file.exists()) {
            //TODO: Log this error?
        }

        final Calendar endingTime = Calendar.getInstance();
        endingTime.setTime(archivalTime.getTime());
        
        try {
            final FileWriter fout = new FileWriter(file);
            boolean done = false;
            
            while (!done) {
                final Feed feedToArchive = feedAdapter.getFeedByDateRange(archivalTime, endingTime);
                
                for (Entry e : feedToArchive.getEntries()) {
                    e.writeTo(fout);
                }
                
                done = feedToArchive.getEntries().isEmpty();
                archivalTime.roll(Calendar.MILLISECOND, getArchivalInterval());
            }
            
            fout.close();
        } catch (IOException ioe) {
            //TODO: Log this
        }
    }

    @Override
    public int getArchivalInterval() {
        return archivalInterval;
    }

    @Override
    public void setArchivalInterval(int archivalIntervalInMiliseconds) {
        archivalInterval = archivalIntervalInMiliseconds;
    }

    @Override
    public AdapterResponse<Feed> getArchivedFeed(RequestContext request, Calendar date) {
        final String destinationFile = StringUtilities.join(
                archiveDirectoryRoot,
                archiveDirectoryRoot.endsWith("/") ? "" : "/",
                date.get(Calendar.YEAR), "/",
                date.get(Calendar.MONTH), "/",
                date.get(Calendar.DAY_OF_MONTH), "/",
                date.get(Calendar.HOUR_OF_DAY), ".archive.xml");

        final File archive = new File(destinationFile);

        try {
            final Document<Feed> feedDoc = adapterTools.getAtomParser().parse(new FileInputStream(archive));
            return ResponseBuilder.found(feedDoc.getRoot());
        } catch (ParseException pe) {
            throw log.newException("Parsing archive failed. Reason: " + pe.getMessage(), pe, ArchiveProcessingException.class);
        } catch (FileNotFoundException fnfe) {
            throw log.newException("Archive not found.", fnfe, ArchiveProcessingException.class);
        }
    }
}
