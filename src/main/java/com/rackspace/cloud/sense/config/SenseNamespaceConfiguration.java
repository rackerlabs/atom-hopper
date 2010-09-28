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
import com.rackspace.cloud.sense.config.SenseServiceConfiguration;
import com.rackspace.cloud.sense.config.v1_0.Namespace;

/**
 *
 * @author John Hopper
 */
public class SenseNamespaceConfiguration extends AbstractResourceMap<SenseServiceConfiguration> {

    private final Namespace namespace;

    public SenseNamespaceConfiguration(String fullUri, String baseUrn, Namespace namespace) {
        super(fullUri, baseUrn);

        this.namespace = namespace;
    }

    public String getTitle() {
        return namespace.getTitle();
    }
}
