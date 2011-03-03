package com.rackspace.cloud.sense.response;

import com.rackspace.cloud.commons.util.http.HttpStatusCode;
import java.util.EnumMap;
import java.util.Map;

public class FeedSourceAdapterResponse<T> implements AdapterResponse<T> {

    public static final HttpStatusCode DEFAULT_HTTP_STATUS_CODE = HttpStatusCode.OK;

    private final T responseBody;
    private final HttpStatusCode statusCode;
    private final String message;
    
    private Map<ResponseParameter, String> parameters;

    public FeedSourceAdapterResponse(T responseBody) {
        this(responseBody, DEFAULT_HTTP_STATUS_CODE, "");
    }

    public FeedSourceAdapterResponse(T responseBody, HttpStatusCode statusCode, String message) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.message = message;
    }

    /**
     * Performs a lazy get if the map has not been initialized yet since most
     * responses will not include parameters
     * 
     * @return
     */
    public synchronized Map<ResponseParameter, String> getParameters() {
        if (parameters == null) {
            parameters = new EnumMap<ResponseParameter, String>(ResponseParameter.class);
        }

        return parameters;
    }

    @Override
    public String getParameter(ResponseParameter key) {
        return getParameters().get(key);
    }

    @Override
    public AdapterResponse<T> withParameter(ResponseParameter key, Object value) {
        getParameters().put(key, value.toString());

        return this;
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
