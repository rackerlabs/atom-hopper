package net.jps.atom.hopper.adapter;

import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import net.jps.atom.hopper.response.FeedSourceAdapterResponse;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.springframework.http.HttpStatus;

/**
 * Utility class designed to make generating an AdapterResponse easy
 */
public final class ResponseBuilder {

    private ResponseBuilder() {
    }

    public static <T> AdapterResponse<T> notFound() {
        return notFound("");
    }

    public static <T> AdapterResponse<T> notFound(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatus.NOT_FOUND, message);
    }

    public static AdapterResponse<EmptyBody> ok() {
        return new FeedSourceAdapterResponse<EmptyBody>(EmptyBody.getInstance(), HttpStatus.OK, "");
    }

    public static AdapterResponse<Feed> found(Feed f) {
        return new FeedSourceAdapterResponse<Feed>(f, HttpStatus.OK, "");
    }

    public static AdapterResponse<Categories> found(Categories c) {
        return new FeedSourceAdapterResponse<Categories>(c, HttpStatus.OK, "");
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

    public static <T> AdapterResponse<T> reply(HttpStatus status, T payload) {
        return reply(status, payload, null);
    }

    public static AdapterResponse<EmptyBody> reply(HttpStatus status) {
        return reply(status, EmptyBody.getInstance(), null);
    }

    public static <T> AdapterResponse<T> error() {
        return error("");
    }

    public static <T> AdapterResponse<T> error(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public static AdapterResponse<Entry> updated(Entry e) {
        return updated(e, "");
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
