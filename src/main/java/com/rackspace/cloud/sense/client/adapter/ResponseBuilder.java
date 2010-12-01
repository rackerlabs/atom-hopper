package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.commons.util.http.HttpStatusCode;
import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.FeedSourceAdapterResponse;
import com.rackspace.cloud.sense.domain.response.AdapterResponse;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

public class ResponseBuilder {

    public static <T> AdapterResponse<T> notFound() {
        return new FeedSourceAdapterResponse<T>(null, HttpStatusCode.NOT_FOUND, "");
    }

    public static <T> AdapterResponse<T> notFound(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatusCode.NOT_FOUND, "");
    }

    public static AdapterResponse<EmptyBody> ok() {
        return new FeedSourceAdapterResponse<EmptyBody>(EmptyBody.getInstance(), HttpStatusCode.OK, "");
    }

    public static AdapterResponse<Feed> found(Feed f) {
        return new FeedSourceAdapterResponse<Feed>(f, HttpStatusCode.OK, "");
    }

    public static AdapterResponse<Entry> found(Entry e) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatusCode.OK, "");
    }

    public static AdapterResponse<Entry> updated(Entry e) {
        return updated(e, "");
    }

    public static AdapterResponse<Entry> updated(Entry e, String message) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatusCode.ACCEPTED, message);
    }

    public static AdapterResponse<Entry> created(Entry e) {
        return created(e, "");
    }

    public static AdapterResponse<Entry> created(Entry e, String message) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatusCode.CREATED, message);
    }
}
