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

import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.FeedSourceAdapterResponse;
import com.rackspace.cloud.sense.domain.response.GenericAdapterResponse;
import com.rackspace.cloud.util.http.HttpStatusCode;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

/**
 *
 * @author zinic
 */
public class ResponseBuilder {

    public static <T> GenericAdapterResponse<T> notFound() {
        return new FeedSourceAdapterResponse<T>(null, HttpStatusCode.NOT_FOUND, "");
    }

    public static <T> GenericAdapterResponse<T> notFound(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatusCode.NOT_FOUND, "");
    }

    public static GenericAdapterResponse<EmptyBody> ok() {
        return new FeedSourceAdapterResponse<EmptyBody>(EmptyBody.getInstance(), HttpStatusCode.OK, "");
    }

    public static GenericAdapterResponse<Feed> found(Feed f) {
        return new FeedSourceAdapterResponse<Feed>(f, HttpStatusCode.OK, "");
    }

    public static GenericAdapterResponse<Entry> found(Entry e) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatusCode.OK, "");
    }

    public static GenericAdapterResponse<Entry> updated(Entry e) {
        return updated(e, "");
    }

    public static GenericAdapterResponse<Entry> updated(Entry e, String message) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatusCode.ACCEPTED, message);
    }

    public static GenericAdapterResponse<Entry> created(Entry e) {
        return created(e, "");
    }

    public static GenericAdapterResponse<Entry> created(Entry e, String message) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatusCode.CREATED, message);
    }
}
