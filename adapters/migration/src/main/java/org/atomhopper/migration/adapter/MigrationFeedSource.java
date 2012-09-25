package org.atomhopper.migration.adapter;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.migration.domain.MigrationReadFrom;
import org.atomhopper.response.AdapterResponse;

import java.util.Map;

public class MigrationFeedSource implements FeedSource {

    private final FeedSource oldFeedSource;
    private final FeedSource newFeedSource;
    private final MigrationReadFrom readFrom;

    public MigrationFeedSource(FeedSource oldFeedSource, FeedSource newFeedSource, MigrationReadFrom readFrom) {
        this.oldFeedSource = oldFeedSource;
        this.newFeedSource = newFeedSource;
        this.readFrom = readFrom;
    }

    @Override
    public FeedInformation getFeedInformation() {
        return readFrom == MigrationReadFrom.NEW ? newFeedSource.getFeedInformation()
                : oldFeedSource.getFeedInformation();
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        return readFrom == MigrationReadFrom.NEW ? newFeedSource.getFeed(getFeedRequest)
                : oldFeedSource.getFeed(getFeedRequest);
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        return readFrom == MigrationReadFrom.NEW ? newFeedSource.getEntry(getEntryRequest)
                : oldFeedSource.getEntry(getEntryRequest);
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
