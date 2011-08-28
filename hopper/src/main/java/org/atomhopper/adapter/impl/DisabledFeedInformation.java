package org.atomhopper.adapter.impl;

import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;

public final class DisabledFeedInformation implements FeedInformation {

    private static final DisabledFeedInformation INSTANCE = new DisabledFeedInformation();
    
    public static DisabledFeedInformation getInstance() {
        return INSTANCE;
    }

    private DisabledFeedInformation() {
    }
    
    @Override
    public String getId(FeedRequest feedRequest) {
        return "atomhopper:no-id";
    }

    @Override
    public Categories getCategories(GetCategoriesRequest getCategoriesRequest) {
        return getCategoriesRequest.newCategories();
    }
}
