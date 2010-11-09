/*
 *  Copyright 2010 zinic.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense.domain.response;

import com.rackspace.cloud.util.http.HttpStatusCode;

/**
 *
 * @author zinic
 */
public class FeedSourceAdapterResponse<T> implements GenericAdapterResponse<T> {

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
