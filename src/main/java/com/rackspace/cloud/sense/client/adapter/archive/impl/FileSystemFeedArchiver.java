/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.client.adapter.archive.impl;

import com.rackspace.cloud.sense.client.adapter.AdapterTools;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import com.rackspace.cloud.util.StringUtilities;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class FileSystemFeedArchiver implements FeedArchiver {

    public static final String ARCHIVE_FILE_EXTENSION = ".archive.xml";

    private final String archiveDirectoryRoot;
    private AdapterTools adapterTools;

    public FileSystemFeedArchiver(String archiveDirectoryRoot) {
        this.archiveDirectoryRoot = archiveDirectoryRoot;
    }

    @Override
    public void init(AdapterTools tools, FeedSourceAdapter feedSourceAdapter) {
        this.adapterTools = tools;
    }

    @Override
    public void archiveFeed(Feed feed, Calendar date) {
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
                //TODO: Log directory creation failure
            }
        } else if (file.exists()) {
            //TODO: Log this error
        }

        try {
            final FileWriter fout = new FileWriter(file);
            feed.writeTo(fout);
            fout.close();
        } catch (IOException ioe) {
            //TODO: Log this
        }
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

        adapterTools.getAtomParser();

        throw new UnsupportedOperationException("Not supported yet.");
    }
}
