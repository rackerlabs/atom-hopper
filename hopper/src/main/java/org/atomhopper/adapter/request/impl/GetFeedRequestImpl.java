package org.atomhopper.adapter.request.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.atomhopper.adapter.request.AbstractClientRequest;
import org.atomhopper.adapter.request.GetFeedRequest;
import org.atomhopper.adapter.request.RequestQueryParameter;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 *
 */
public class GetFeedRequestImpl extends AbstractClientRequest implements GetFeedRequest {

    private final List<String> categories;

    public GetFeedRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);

        categories = getCategories(abderaRequestContext);
    }

    private List<String> getCategories(RequestContext requestContext) {
        final List<String> categoryList = requestContext.getParameters(RequestQueryParameter.CATEGORIES.toString());

        return Collections.unmodifiableList(categoryList != null ? categoryList : new LinkedList<String>());
    }

    @Override
    public List<String> getCategories() {
        return categories;
    }

    @Override
    public String getPageMarker() {
        return getRequestContext().getParameter(RequestQueryParameter.MARKER.toString());
    }

    @Override
    public Feed newFeed() {
        return getRequestContext().getAbdera().newFeed();
    }
}
