package net.jps.atom.hopper.adapter;

import com.rackspace.cloud.commons.util.http.HttpStatusCode;
import net.jps.atom.hopper.response.EmptyBody;
import net.jps.atom.hopper.response.FeedSourceAdapterResponse;
import net.jps.atom.hopper.response.AdapterResponse;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;

public class ResponseBuilder {

    public static <T> AdapterResponse<T> notFound() {
        return notFound("");
    }

    public static <T> AdapterResponse<T> notFound(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatusCode.NOT_FOUND, message);
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

    public static <T> AdapterResponse<T> error() {
        return error("");
    }

    public static <T> AdapterResponse<T> error(String message) {
        return new FeedSourceAdapterResponse<T>(null, HttpStatusCode.INTERNAL_SERVER_ERROR, message);
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
