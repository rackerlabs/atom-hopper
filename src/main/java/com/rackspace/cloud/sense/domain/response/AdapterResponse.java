package com.rackspace.cloud.sense.domain.response;

import com.rackspace.cloud.util.http.HttpStatusCode;

public interface AdapterResponse<T> {

    T getBody();

    String getParameter(ResponseParameter name);

    void setParameter(ResponseParameter name, String value);

    String getMessage();

    HttpStatusCode getResponseStatus();
}
