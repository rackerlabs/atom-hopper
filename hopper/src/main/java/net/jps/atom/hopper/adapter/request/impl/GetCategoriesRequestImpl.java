/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter.request.impl;

import net.jps.atom.hopper.adapter.request.AbstractClientRequest;
import net.jps.atom.hopper.adapter.request.GetCategoriesRequest;
import org.apache.abdera.model.Categories;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class GetCategoriesRequestImpl extends AbstractClientRequest implements GetCategoriesRequest {

    public GetCategoriesRequestImpl(RequestContext abderaRequestContext) {
        super(abderaRequestContext);
    }

    @Override
    public Categories newCategories() {
        return getRequestContext().getAbdera().newCategories();
    }
}
