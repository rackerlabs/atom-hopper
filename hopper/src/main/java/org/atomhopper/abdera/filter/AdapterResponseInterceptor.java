package org.atomhopper.abdera.filter;

import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.response.AdapterResponse;

/**
 *
 * 
 */
public interface AdapterResponseInterceptor<T> {

    void process(RequestContext rc, AdapterResponse<T> adapterResponse);
}
