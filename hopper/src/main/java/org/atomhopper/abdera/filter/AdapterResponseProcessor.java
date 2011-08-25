package org.atomhopper.abdera.filter;

import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * 
 */
public interface AdapterResponseProcessor<T> {

    void process(RequestContext rc, AdapterResponse<T> adapterResponse);
}
