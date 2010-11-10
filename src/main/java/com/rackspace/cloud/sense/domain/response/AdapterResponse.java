package com.rackspace.cloud.sense.domain.response;

import com.rackspace.cloud.util.http.HttpStatusCode;

public interface AdapterResponse<T> {

    T getBody();

    String getParameter(ResponseParameter name);

    /**
     * Setting a parameter will take the value's toString() value and use it to
     * represent the value.
     * 
     * @param name
     * @param value
     * @return
     */
    AdapterResponse<T> withParameter(ResponseParameter name, Object value);

    String getMessage();

    HttpStatusCode getResponseStatus();
}
