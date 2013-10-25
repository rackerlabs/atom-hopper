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

import java.net.URL;
import java.util.Map;

public class MigrationFeedSource implements FeedSource {

    private FeedSource oldFeedSource;
    private FeedSource newFeedSource;
    private MigrationReadFrom readFrom;

    public void setOldFeedSource(FeedSource oldFeedSource) {
        this.oldFeedSource = oldFeedSource;
    }

    public void setNewFeedSource(FeedSource newFeedSource) {
        this.newFeedSource = newFeedSource;
    }

    public void setReadFrom(MigrationReadFrom readFrom) {
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
    public void setCurrentUrl( URL urlCurrent ) {

        // No op - migration adapter isn't used on archive feeds
    }

    @Override
    public void setArchiveUrl( URL url ) {

        oldFeedSource.setArchiveUrl( url );
        newFeedSource.setArchiveUrl( url );
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
