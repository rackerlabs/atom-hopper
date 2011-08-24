package org.atomhopper.abdera.response;

import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;

/**
 *
 * 
 */
public interface ResponseHandler<T> {

    ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<T> adapterResponse);
}
