package org.atomhopper.adapter.request.adapter.impl;

import java.util.Collections;
import java.util.List;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.adapter.request.RequestQueryParameter;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.feed.AbstractFeedRequest;

public class GetFeedRequestImpl extends AbstractFeedRequest implements GetFeedRequest {

    public GetFeedRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public List<String> getCategories() {
        final List<String> categoryList = getRequestParameters(RequestQueryParameter.CATEGORIES.toString());

        return Collections.unmodifiableList(categoryList != null ? categoryList : Collections.EMPTY_LIST);
    }

    @Override
    public String getPageSize() {
        return getRequestParameter(RequestQueryParameter.PAGE_LIMIT.toString());
    }

    @Override
    public String getPageMarker() {
        return getRequestParameter(RequestQueryParameter.MARKER.toString());
    }

    @Override
    public Feed newFeed() {
        return getAbdera().newFeed();
    }
}
