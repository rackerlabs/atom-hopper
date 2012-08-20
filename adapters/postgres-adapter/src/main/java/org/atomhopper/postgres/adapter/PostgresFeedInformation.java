package org.atomhopper.postgres.adapter;

import javax.sql.DataSource;
import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class PostgresFeedInformation implements FeedInformation {
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getId(FeedRequest feedRequest) {
        return "TODO";
        //return feedRepository.getFeed(feedRequest.getFeedName()).getFeedId();
    }

    @Override
    @NotImplemented
    public Categories getCategories(GetCategoriesRequest getCategoriesRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}