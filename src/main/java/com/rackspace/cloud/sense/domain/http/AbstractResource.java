/*
 *  Copyright 2010 Rackspace.
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
package com.rackspace.cloud.sense.domain.http;

/**
 *
 * @author John Hopper
 */
public class AbstractResource implements Resource {

    private final String fullUri;
    private final String baseUrn;

    public AbstractResource(String fullUri, String baseUrn) {
        this.fullUri = fullUri;
        this.baseUrn = sanitizeBaseUrn(baseUrn.trim());
    }

    private String sanitizeBaseUrn(String urn) {
        final String newUrn = urn.endsWith("/") ? urn.substring(0, urn.length() - 1) : urn;

        return newUrn.startsWith("/") ? newUrn : "/" + newUrn;
    }

    @Override
    public String getBaseUrn() {
        return baseUrn;
    }

    @Override
    public String getFullUri() {
        return fullUri;
    }
}
