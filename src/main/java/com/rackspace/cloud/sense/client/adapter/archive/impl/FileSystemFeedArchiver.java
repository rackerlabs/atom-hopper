/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.client.adapter.archive.impl;

import com.rackspace.cloud.commons.logging.Logger;
import com.rackspace.cloud.commons.logging.RCLogger;
import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.sense.client.adapter.AdapterTools;
import com.rackspace.cloud.sense.client.adapter.ResponseBuilder;
import com.rackspace.cloud.sense.client.adapter.archive.ArchiveProcessingException;
import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.ParseException;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class FileSystemFeedArchiver implements FeedArchiver {
    
    private static final Logger log = new RCLogger(FileSystemFeedArchiver.class);

    public static final String ARCHIVE_FILE_EXTENSION = ".archive.xml";
    private final String archiveDirectoryRoot;
    private AdapterTools adapterTools;

    public FileSystemFeedArchiver(String archiveDirectoryRoot) {
        this.archiveDirectoryRoot = archiveDirectoryRoot;
    }

    @Override
    public void init(AdapterTools tools) {
        this.adapterTools = tools;
    }

    @Override
    public void archiveFeed(Calendar date) {
        final String destinationDirectory = StringUtilities.join(
                archiveDirectoryRoot,
                archiveDirectoryRoot.endsWith("/") ? "" : "/",
                date.get(Calendar.YEAR), "/",
                date.get(Calendar.MONTH), "/",
                date.get(Calendar.DAY_OF_MONTH));

        final String destinationFile = StringUtilities.join(
                destinationDirectory, "/",
                date.get(Calendar.HOUR_OF_DAY), ARCHIVE_FILE_EXTENSION);

        final File dir = new File(destinationDirectory);
        final File file = new File(destinationFile);

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.error("Unable to create archive directory, \"" + dir.getAbsolutePath() + "\"  --  Please check directory permissions");
            }
        } else if (file.exists()) {
            //TODO: Log this error?
        }

        try {
            final FileWriter fout = new FileWriter(file);
//            feed.writeTo(fout);
            fout.close();
        } catch (IOException ioe) {
            //TODO: Log this
        }
    }

    @Override
    public long getArchivalInterval() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setArchivalInterval(long archivalIntervalInMiliseconds) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
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
