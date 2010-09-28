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
package com.rackspace.cloud.sense.config;

import com.rackspace.cloud.sense.domain.http.AbstractResource;
import com.rackspace.cloud.sense.config.v1_0.Feed;

/**
 *
 * @author John Hopper
 */
public class SenseFeedConfiguration extends AbstractResource {

    private final Feed baseConfiguration;

    public SenseFeedConfiguration(String fullUri, String baseUrn, Feed baseConfiguration) {
        super(fullUri, baseUrn);
        
        this.baseConfiguration = baseConfiguration;
    }

    public String getTitle() {
        return baseConfiguration.getTitle();
    }

    public String getResource() {
        return baseConfiguration.getResource();
    }

    public String getAuthor() {
        return baseConfiguration.getAuthor().getName();
    }
}
