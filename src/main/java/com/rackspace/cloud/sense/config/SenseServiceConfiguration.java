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

import com.rackspace.cloud.sense.domain.http.AbstractResourceMap;
import com.rackspace.cloud.sense.config.SenseFeedConfiguration;
import com.rackspace.cloud.sense.config.v1_0.Service;

/**
 *
 * @author John Hopper
 */
public class SenseServiceConfiguration extends AbstractResourceMap<SenseFeedConfiguration> {

    private final Service serviceConfig;

    public SenseServiceConfiguration(String fullUri, String baseUrn, Service serviceConfig) {
        super(fullUri, baseUrn);

        this.serviceConfig = serviceConfig;
    }

    public String getTitle() {
        return serviceConfig.getTitle();
    }

    public String getAdapterRef() {
        return serviceConfig.getAdapterRef();
    }

    public String getAdapterClass() {
        return serviceConfig.getAdapterClass();
    }
}
