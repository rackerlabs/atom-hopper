package net.jps.atom.hopper.adapter.hibernate.impl;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.adapter.request.GetCategoriesRequest;
import net.jps.atom.hopper.adapter.request.GetEntryRequest;
import net.jps.atom.hopper.adapter.request.GetFeedRequest;
import net.jps.atom.hopper.response.AdapterResponse;
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
