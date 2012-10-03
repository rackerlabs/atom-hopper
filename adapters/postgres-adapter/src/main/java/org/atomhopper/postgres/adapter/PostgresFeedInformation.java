package org.atomhopper.postgres.adapter;

import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;

public class PostgresFeedInformation implements FeedInformation {
    //private DataSource dataSource;

    //public void setDataSource(DataSource dataSource) {
    //    this.dataSource = dataSource;
    //}

    @Override
    public String getId(FeedRequest feedRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    @NotImplemented
    public Categories getCategories(GetCategoriesRequest getCategoriesRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}