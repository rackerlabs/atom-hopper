package org.atomhopper.postgres.adapter;

import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;
import org.atomhopper.dbal.FeedRepository;

public class PostgresFeedInformation implements FeedInformation {

    private final FeedRepository feedRepository;

    public PostgresFeedInformation(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    @Override
    public String getId(FeedRequest feedRequest) {
        return feedRepository.getFeed(feedRequest.getFeedName()).getFeedId();
    }

    @Override @NotImplemented
    public Categories getCategories(GetCategoriesRequest getCategoriesRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}