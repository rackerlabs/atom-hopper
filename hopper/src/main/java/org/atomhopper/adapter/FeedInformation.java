package org.atomhopper.adapter;

import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;

public interface FeedInformation {

    /**
     * Requests the ID of the underlying feed as a string value.
     * 
     * @return 
     */
    String getId(FeedRequest getFeedRequest);

    /**
     * Retrieves a list of categories supported by this feed source.
     *
     * A workspace provider may poll all of its associated feeds for their
     * categories and then aggregate them into a service document.
     *
     * @return
     */
    Categories getCategories(GetCategoriesRequest getCategoriesRequest);
}
