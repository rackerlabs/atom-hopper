package org.atomhopper.mongodb.adapter;

import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

public class MongodbFeedInformation implements FeedInformation {

    //private MongoTemplate mongoTemplate;

    //public MongodbFeedInformation(MongoTemplate mongoTemplate) {
    //    this.mongoTemplate = mongoTemplate;
    //}

    @Override
    public String getId(FeedRequest feedRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override @NotImplemented
    public Categories getCategories(GetCategoriesRequest getCategoriesRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}