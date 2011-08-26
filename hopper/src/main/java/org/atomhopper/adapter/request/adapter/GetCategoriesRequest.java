package org.atomhopper.adapter.request.adapter;

import org.apache.abdera.model.Categories;
import org.atomhopper.adapter.request.ClientRequest;

public interface GetCategoriesRequest extends ClientRequest {

    Categories newCategories();
}
