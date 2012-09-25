package org.atomhopper.migration.adapter;

import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;
import org.atomhopper.migration.domain.MigrationReadFrom;

public class MigrationFeedInformation implements FeedInformation {

    private final FeedInformation oldFeedInformation;
    private final FeedInformation newFeedInformation;
    private final MigrationReadFrom readFrom;

    public MigrationFeedInformation(FeedInformation oldFeedInformation,
                                    FeedInformation newFeedInformation,
                                    MigrationReadFrom readFrom) {

        this.oldFeedInformation = oldFeedInformation;
        this.newFeedInformation = newFeedInformation;
        this.readFrom = readFrom;
    }

    @Override
    public String getId(FeedRequest getFeedRequest) {
        return readFrom == MigrationReadFrom.NEW ? newFeedInformation.getId(getFeedRequest)
                : oldFeedInformation.getId(getFeedRequest);
    }

    @Override
    @NotImplemented
    public Categories getCategories(GetCategoriesRequest getCategoriesRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
