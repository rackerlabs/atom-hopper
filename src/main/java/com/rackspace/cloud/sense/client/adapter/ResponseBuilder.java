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
package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.EntryResponse;
import com.rackspace.cloud.sense.domain.response.FeedResponse;
import com.rackspace.cloud.sense.domain.http.HttpResponseCode;
import com.rackspace.cloud.sense.util.Utilities;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public class ResponseBuilder {

    public static EntryResponse notFound() {
        return notFound(new Object[0]);
    }

    public static EntryResponse notFound(Object... messageArray) {
        return new EntryResponse(HttpResponseCode.NOT_FOUND, Utilities.join(messageArray), null);
    }

    public static EntryResponse ok() {
        return new EntryResponse(HttpResponseCode.OK, null, null);
    }

    public static FeedResponse ok(Feed f) {
        return new FeedResponse(HttpResponseCode.OK, null, f);
    }

    public static EntryResponse ok(Entry e) {
        return new EntryResponse(HttpResponseCode.OK, null, e);
    }

    public static EntryResponse created(Entry e) {
        return new EntryResponse(HttpResponseCode.CREATED, null, e);
    }
}
