package org.atomhopper.adapter;

import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.response.FeedSourceAdapterResponse;
import org.springframework.http.HttpStatus;

/**
 * Utility class designed to make generating an AdapterResponse easy
 */
public final class ResponseBuilder {

    private ResponseBuilder() {
    }

    public static <T> AdapterResponse<T> notImplemented(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatus.METHOD_NOT_ALLOWED, message);
    }
    
    public static <T> AdapterResponse<T> badRequest(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatus.BAD_REQUEST, message);
    }
    
    public static <T> AdapterResponse<T> notFound() {
        return notFound("");
    }

    public static <T> AdapterResponse<T> notFound(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatus.NOT_FOUND, message);
    }

    public static AdapterResponse<Feed> found(Feed f) {
        return new FeedSourceAdapterResponse<Feed>(f, HttpStatus.OK, "");
    }

    public static AdapterResponse<Entry> found(Entry e) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatus.OK, "");
    }

    public static <T> AdapterResponse<T> reply(HttpStatus status, T payload, String message) {
        return new FeedSourceAdapterResponse<T>(payload, status, message != null ? message : "");
    }

    public static <T> AdapterResponse<T> reply(HttpStatus status, String message) {
        return reply(status, null, message);
    }

    public static <T> AdapterResponse<T> error(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static AdapterResponse<Entry> updated(Entry e, String message) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatus.ACCEPTED, message);
    }

    public static AdapterResponse<Entry> created(Entry e) {
        return created(e, "");
    }

    public static AdapterResponse<Entry> created(Entry e, String message) {
        return new FeedSourceAdapterResponse<Entry>(e, HttpStatus.CREATED, message);
    }
}
