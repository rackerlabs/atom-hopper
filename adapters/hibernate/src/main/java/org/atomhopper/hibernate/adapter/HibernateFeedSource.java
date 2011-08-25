package org.atomhopper.hibernate.adapter;

import java.util.Calendar;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.request.GetCategoriesRequest;
import org.atomhopper.adapter.request.GetEntryRequest;
import org.atomhopper.adapter.request.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

public class HibernateFeedSource implements FeedSource {
    
    @Override
    public AdapterResponse<Categories> getCategories(GetCategoriesRequest getCategoriesRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Feed getFeedByDateRange(Calendar startingEntryDate, Calendar lastEntryDate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
