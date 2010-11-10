package com.rackspace.cloud.sense.domain.response;

import com.rackspace.cloud.util.http.HttpStatusCode;

public interface AdapterResponse<T> {

    T getBody();

    String getMessage();

    HttpStatusCode getResponseStatus();
}
