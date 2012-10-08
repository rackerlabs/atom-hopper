package org.atomhopper.migration.adapter;

import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.migration.domain.MigrationReadFrom;
import org.atomhopper.migration.domain.MigrationWriteTo;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class MigrationFeedPublisher implements FeedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(MigrationFeedPublisher.class);
    private static final String UUID_URI_SCHEME = "urn:uuid:";

    private FeedPublisher oldFeedPublisher;
    private FeedPublisher newFeedPublisher;
    private MigrationWriteTo writeTo;
    private MigrationReadFrom readFrom;

    private boolean allowOverrideId = false;
    private boolean allowOverrideDate = false;

    public void setOldFeedPublisher(FeedPublisher oldFeedPublisher) {
        this.oldFeedPublisher = oldFeedPublisher;
    }

    public void setNewFeedPublisher(FeedPublisher newFeedPublisher) {
        this.newFeedPublisher = newFeedPublisher;
    }

    public void setWriteTo(MigrationWriteTo writeTo) {
        this.writeTo = writeTo;
    }

    public void setReadFrom(MigrationReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    public void setAllowOverrideId(boolean allowOverrideId) {
        this.allowOverrideId = allowOverrideId;
    }

    public void setAllowOverrideDate(boolean allowOverrideDate) {
        this.allowOverrideDate = allowOverrideDate;
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {

        PersistedEntry entry = new PersistedEntry();

        // If allowOverrideId is false then set the Id
        // Also set the id if allowOverrideId is true, but no Id was sent in the entry
        if (!allowOverrideId || postEntryRequest.getEntry().getId() == null) {
            postEntryRequest.getEntry().setId(UUID_URI_SCHEME + UUID.randomUUID().toString());
        }

        // If allowOverrideDate is false then set the DateLastUpdated
        // Also set the DateLastUpdated if allowOverrideDate is true, but no DateLastUpdated was sent in the entry
        if (!allowOverrideDate || postEntryRequest.getEntry().getUpdated() == null) {
            postEntryRequest.getEntry().setUpdated(entry.getDateLastUpdated());
        }

        switch (writeTo) {
            case OLD:
                return oldFeedPublisher.postEntry(postEntryRequest);
            case NEW:
                return newFeedPublisher.postEntry(postEntryRequest);
            case BOTH:
            default:
                switch (readFrom) {
                    case NEW:

                        AdapterResponse<Entry> newEntry = newFeedPublisher.postEntry(postEntryRequest);

                        try {
                            oldFeedPublisher.postEntry(postEntryRequest);
                        } catch (Exception ex) {
                            LOG.error("Error writing entry to OLD feed. EntryId=" + postEntryRequest.getEntry().getId());
                        }

                        return newEntry;

                    case OLD:
                    default:
                        AdapterResponse<Entry> oldEntry = oldFeedPublisher.postEntry(postEntryRequest);

                        try {
                            newFeedPublisher.postEntry(postEntryRequest);
                        } catch (Exception ex) {
                            LOG.error("Error writing entry to NEW feed. EntryId=" + postEntryRequest.getEntry().getId());
                        }

                        return oldEntry;
                }

        }
    }

    @Override
    @NotImplemented
    public AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    @NotImplemented
    public AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
