package org.atomhopper.migration.adapter;

import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.FeedInformation;
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

    private final FeedPublisher oldFeedPublisher;
    private final FeedPublisher newFeedPublisher;
    private final MigrationWriteTo writeTo;
    private final MigrationReadFrom readFrom;

    public MigrationFeedPublisher(FeedPublisher oldFeedPublisher,
                                  FeedPublisher newFeedPublisher,
                                  MigrationWriteTo writeTo,
                                  MigrationReadFrom readFrom) {
        this.oldFeedPublisher = oldFeedPublisher;
        this.newFeedPublisher = newFeedPublisher;
        this.writeTo = writeTo;
        this.readFrom = readFrom;
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {

        PersistedEntry entry = new PersistedEntry();

        postEntryRequest.getEntry().setId(UUID_URI_SCHEME + UUID.randomUUID().toString());
        postEntryRequest.getEntry().setPublished(entry.getDateLastUpdated());
        postEntryRequest.getEntry().setUpdated(entry.getDateLastUpdated());

        //TODO: More Here


        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
