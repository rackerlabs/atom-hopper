package com.rackspace.cloud.sense.response;

import com.rackspace.cloud.commons.util.http.HttpStatusCode;

public interface AdapterResponse<T> {

    /**
     * 
     * @return 
     */
    T getBody();

    /**
     * 
     * @param name
     * @return 
     */
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

    /**
     * 
     * @return 
     */
    String getMessage();

    /**
     * 
     * @return 
     */
    HttpStatusCode getResponseStatus();
}
