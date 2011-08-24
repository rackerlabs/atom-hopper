package org.atomhopper.response;

import org.springframework.http.HttpStatus;

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
    HttpStatus getResponseStatus();
}
