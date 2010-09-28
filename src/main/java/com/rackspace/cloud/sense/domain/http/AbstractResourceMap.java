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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author John Hopper
 */
public abstract class AbstractResourceMap<T> extends AbstractResource implements ResourceMap<T> {

    private final Map<String, T> resourceMap;

    public AbstractResourceMap(String fullUri, String baseUrn) {
        super(fullUri, baseUrn);

        resourceMap = new HashMap<String, T>();
    }

    @Override
    public void add(String id, T T) {
        resourceMap.put(id, T);
    }

    @Override
    public Collection<T> getAll() {
        return resourceMap.values();
    }

    @Override
    public T getById(String id) {
        return resourceMap.get(id);
    }
}
