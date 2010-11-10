package com.rackspace.cloud.sense.domain.response;

import com.rackspace.cloud.util.http.HttpStatusCode;

public class FeedSourceAdapterResponse<T> implements AdapterResponse<T> {

    public static final HttpStatusCode DEFAULT_HTTP_STATUS_CODE = HttpStatusCode.OK;
    
    private final T responseBody;
    private final HttpStatusCode statusCode;
    private final String message;

    public FeedSourceAdapterResponse(T responseBody) {
        this(responseBody, DEFAULT_HTTP_STATUS_CODE, "");
    }

    public FeedSourceAdapterResponse(T responseBody, HttpStatusCode statusCode, String message) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public T getBody() {
        return responseBody;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatusCode getResponseStatus() {
        return statusCode;
    }
}
