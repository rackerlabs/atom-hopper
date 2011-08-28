package org.atomhopper.abdera.response;

import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.atomhopper.abdera.filter.AdapterResponseInterceptor;

public interface ResponseHandler<T> {

    void addResponseInterceptor(AdapterResponseInterceptor adapterResponseInterceptor);

    void clearResponseInterceptors();

    ResponseContext handleResponse(RequestContext rc, AdapterResponse<T> adapterResponse);
}
