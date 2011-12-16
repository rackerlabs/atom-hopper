package org.atomhopper.response;

import org.apache.abdera.util.EntityTag;
import org.springframework.http.HttpStatus;

import java.util.EnumMap;
import java.util.Map;

public class FeedSourceAdapterResponse<T> implements AdapterResponse<T> {

    private static final HttpStatus DEFAULT_HTTP_STATUS_CODE = HttpStatus.OK;

    private final T responseBody;
    private final HttpStatus statusCode;
    private final String message;
    private final Map<ResponseParameter, String> parameters;
    
    private EntityTag etag;
    
    public FeedSourceAdapterResponse(T responseBody) {
        this(responseBody, DEFAULT_HTTP_STATUS_CODE, "");
    }

    public FeedSourceAdapterResponse(T responseBody, HttpStatus statusCode, String message) {
        this.responseBody = responseBody;
        this.statusCode = statusCode;
        this.message = message;
        
        parameters = new EnumMap<ResponseParameter, String>(ResponseParameter.class);
    }

    public Map<ResponseParameter, String> getParameters() {
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
    public HttpStatus getResponseStatus() {
        return statusCode;
    }

    @Override
    public EntityTag getEntityTag() {
        return etag;
    }

    @Override
    public void setEntityTag(EntityTag etag) {
        this.etag = etag;
    }
}
