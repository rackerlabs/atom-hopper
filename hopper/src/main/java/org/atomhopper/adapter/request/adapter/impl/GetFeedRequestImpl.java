package org.atomhopper.adapter.request.adapter.impl;

import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.RequestQueryParameter;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.adapter.request.feed.AbstractFeedRequest;
import org.h2.util.StringUtils;

import java.util.Collections;
import java.util.List;

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
    public String getSearchQuery() {
        return getRequestParameter(RequestQueryParameter.SEARCH.toString());
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

    public String getDirection() {
        final String direction = this.getRequestParameter(RequestQueryParameter.PAGE_DIRECTION.toString());
        return !StringUtils.isNullOrEmpty(direction) ? direction : "forward";
    }

    public String getStartingAt() {
        return this.getRequestParameter(RequestQueryParameter.STARTING_AT.toString());
    }
}
