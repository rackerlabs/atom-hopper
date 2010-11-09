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

import com.rackspace.cloud.sense.domain.http.HttpResponseCode;
import com.rackspace.cloud.util.StringUtilities;


/**
 *
 * @author zinic
 */
public class AbstractResponse implements SimpleResponse {

    private final HttpResponseCode responseCode;
    private final String message;

    public AbstractResponse(HttpResponseCode responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpResponseCode getResponseCode() {
        return responseCode;
    }

    @Override
    public boolean hasMessage() {
        return !StringUtilities.isBlank(message);
    }
}
