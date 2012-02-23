package org.atomhopper.adapter.request.adapter.impl;

import org.apache.abdera.model.Categories;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.AbstractClientRequest;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;

public class GetCategoriesRequestImpl extends AbstractClientRequest implements GetCategoriesRequest {

    public GetCategoriesRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public Categories newCategories() {
        return getAbdera().newCategories();
    }
}
