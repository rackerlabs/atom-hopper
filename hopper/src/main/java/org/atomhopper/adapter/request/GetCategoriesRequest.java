package org.atomhopper.adapter.request;

import org.apache.abdera.model.Categories;

/**
 *
 *
 */
public interface GetCategoriesRequest extends ClientRequest {

    Categories newCategories();
}
