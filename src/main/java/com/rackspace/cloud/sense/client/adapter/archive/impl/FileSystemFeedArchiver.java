/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.client.adapter.archive.impl;

import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapterWrapper;
import com.rackspace.cloud.sense.client.adapter.FeedSourceAdapter;
import com.rackspace.cloud.sense.client.adapter.archive.FeedArchiver;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;
import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.ResponseParameter;
import com.rackspace.cloud.util.StringUtilities;
import com.rackspace.cloud.util.http.HttpStatusCode;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class FileSystemFeedArchiver extends FeedSourceAdapterWrapper implements FeedArchiver {

    private final String archiveDirectoryRoot;
    private final List<EntryInfo> entryList;

    public FileSystemFeedArchiver(FeedSourceAdapter wrappedAdapter, String archiveDirectoryRoot) {
        super(wrappedAdapter);

        this.archiveDirectoryRoot = archiveDirectoryRoot;
        entryList = new LinkedList<EntryInfo>();
    }

    public synchronized void addEntry(String entryId, String entryUri, Entry e) {
        entryList.add(new EntryInfo(entryId, e));
    }

    public synchronized void removeEntry(String entryId) {
        entryList.remove(new EntryInfo(entryId));
    }

    public synchronized List<EntryInfo> cloneEntryList() {
        final List<EntryInfo> clonedList = new LinkedList<EntryInfo>(entryList);
        entryList.clear();

        return clonedList;
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
                date.get(Calendar.HOUR_OF_DAY), ".archive");

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

            for (EntryInfo ei : cloneEntryList()) {
                fout.write(StringUtilities.join("<!-- BEGIN ENTRY: ", ei.getId(), " -->\n"));
                ei.getEntry().writeTo(fout);
                fout.write(StringUtilities.join("<!-- END ENTRY -->\n"));
            }
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
                date.get(Calendar.HOUR_OF_DAY), ".archive");

        final File archive = new File(destinationFile);

//        Abdera.getNewParser()

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> putEntry(RequestContext request, String entryId, Entry entryToUpdate) throws UnsupportedOperationException {
        final AdapterResponse<Entry> response = getFeedSourceAdapter().putEntry(request, entryId, entryToUpdate);

        if (response.getResponseStatus() == HttpStatusCode.OK) {
            addEntry(entryId, request.getUri().toString(), response.getBody());
        }

        return response;
    }

    @Override
    public AdapterResponse<Entry> postEntry(RequestContext request, Entry entryToAdd) throws UnsupportedOperationException {
        final AdapterResponse<Entry> response = getFeedSourceAdapter().postEntry(request, entryToAdd);

        if (response.getResponseStatus() == HttpStatusCode.CREATED) {
            final String entryId = response.getParameter(ResponseParameter.ENTRY_ID);

            if (!StringUtilities.isBlank(entryId)) {
                final String entryUri = request.getUri().toString();
                addEntry(entryId, entryUri + (entryUri.endsWith("/") ? "" : "/") + entryId, response.getBody());
            } else {
                //TODO: Log that an entry id was not returned
            }
        }

        return response;
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(RequestContext request, String id) throws UnsupportedOperationException {
        final AdapterResponse<EmptyBody> response = getFeedSourceAdapter().deleteEntry(request, id);

        if (response.getResponseStatus() == HttpStatusCode.OK) {
            deleteEntry(request, id);
        }

        return response;
    }
}
