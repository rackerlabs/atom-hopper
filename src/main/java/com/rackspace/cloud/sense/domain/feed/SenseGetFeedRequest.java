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
package com.rackspace.cloud.sense.domain.feed;

import com.rackspace.cloud.sense.domain.AbstractRequest;
import org.apache.abdera.protocol.server.RequestContext;

/**
 *
 * @author zinic
 */
public class SenseGetFeedRequest extends AbstractRequest implements GetFeedRequest {

    private final String path;
    private final String feedName;

    public SenseGetFeedRequest(RequestContext requestContext, String path, String feedName) {
        super(requestContext);

        this.path = path;
        this.feedName = feedName;
    }

    @Override
    public String getFeedName() {
        return feedName;
    }

    @Override
    public String getPath() {
        return path;
    }
}
