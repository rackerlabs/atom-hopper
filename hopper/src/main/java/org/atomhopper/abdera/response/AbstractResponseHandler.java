package org.atomhopper.abdera.response;

import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.atomhopper.abdera.filter.AdapterResponseInterceptor;
import org.atomhopper.response.AdapterResponse;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractResponseHandler<T> implements ResponseHandler<T> {

    private final List<AdapterResponseInterceptor<T>> responseInterceptors;
    private final String[] allowedMethods;

    AbstractResponseHandler(String[] allowedMethods, AdapterResponseInterceptor<T>... interceptors) {
        this.allowedMethods = Arrays.copyOf(allowedMethods, allowedMethods.length);
        responseInterceptors = new LinkedList<AdapterResponseInterceptor<T>>(Arrays.asList(interceptors));
    }

    @Override
    public final ResponseContext handleResponse(RequestContext rc, AdapterResponse<T> adapterResponse) {
        processResponse(rc, adapterResponse);

        return handleAdapterResponse(rc, adapterResponse);
    }

    @Override
    public void addResponseInterceptor(AdapterResponseInterceptor adapterResponseInterceptor) {
        responseInterceptors.add(adapterResponseInterceptor);
    }

    @Override
    public void clearResponseInterceptors() {
        responseInterceptors.clear();
    }

    protected abstract ResponseContext handleAdapterResponse(RequestContext rc, AdapterResponse<T> adapterResponse);

    String[] getAllowedHttpMethods() {
        return allowedMethods;
    }

    private void processResponse(RequestContext rc, AdapterResponse<T> adapterResponse) {
        for (AdapterResponseInterceptor<T> processor : responseInterceptors) {
            processor.process(rc, adapterResponse);
        }
    }
}
