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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class FileSystemFeedArchiver extends FeedSourceAdapterWrapper implements FeedArchiver {

    private final List<EntryInfo> entryList;

    public FileSystemFeedArchiver(FeedSourceAdapter wrappedAdapter) {
        super(wrappedAdapter);

        entryList = new LinkedList<EntryInfo>();
    }

    public synchronized void addEntry(String entryId, Entry e) {
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
    public void archiveFeed(Date date, Feed copy) {
        final List<EntryInfo> feedSnapshot = cloneEntryList();

        //TODO: Implement flushing archive to disk
    }

    @Override
    public AdapterResponse<Feed> getArchivedFeed(RequestContext request, Date date) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> putEntry(RequestContext request, String entryId, Entry entryToUpdate) throws UnsupportedOperationException {
        final AdapterResponse<Entry> response = getFeedSourceAdapter().putEntry(request, entryId, entryToUpdate);

        if (response.getResponseStatus() == HttpStatusCode.OK) {
            addEntry(entryId, response.getBody());
        }

        return response;
    }

    @Override
    public AdapterResponse<Entry> postEntry(RequestContext request, Entry entryToAdd) throws UnsupportedOperationException {
        final AdapterResponse<Entry> response = getFeedSourceAdapter().postEntry(request, entryToAdd);

        if (response.getResponseStatus() == HttpStatusCode.CREATED) {
            final String entryId = response.getParameter(ResponseParameter.ENTRY_ID);

            if (!StringUtilities.isBlank(entryId)) {
                addEntry(entryId, response.getBody());
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
