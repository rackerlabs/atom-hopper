package org.atomhopper.abdera.filter;

import java.util.Calendar;
import java.util.TimeZone;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.response.AdapterResponse;


public class FeedPagingProcessor implements AdapterResponseInterceptor<Feed> {
    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed f = adapterResponse.getBody();

        // If there are no entries in the feed
        if (f == null || f.getEntries() == null || f.getEntries().isEmpty()) {
            return;
        }
        // Add an updated element to the feed
        final Calendar localNow = Calendar.getInstance(TimeZone.getDefault());
        localNow.setTimeInMillis(System.currentTimeMillis());
        f.setUpdated(localNow.getTime());
    }
}
