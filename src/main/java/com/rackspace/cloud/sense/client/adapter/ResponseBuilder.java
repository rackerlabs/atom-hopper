package com.rackspace.cloud.sense.client.adapter;

import com.rackspace.cloud.sense.domain.response.EmptyBody;
import com.rackspace.cloud.sense.domain.response.FeedSourceAdapterResponse;
import com.rackspace.cloud.sense.domain.response.GenericAdapterResponse;
import com.rackspace.cloud.util.http.HttpStatusCode;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

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
