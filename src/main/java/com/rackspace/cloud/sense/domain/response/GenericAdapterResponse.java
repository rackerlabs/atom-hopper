package com.rackspace.cloud.sense.domain.response;

import com.rackspace.cloud.util.http.HttpStatusCode;

public interface GenericAdapterResponse<T> {

    T getBody();

    String getMessage();

    HttpStatusCode getResponseStatus();
}
