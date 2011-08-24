package org.atomhopper.abdera.filter;

import org.atomhopper.config.v1_0.Author;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;

public class FeedConfigurationResponseProcessor implements AdapterResponseProcessor<Feed> {
    private FeedConfiguration feedConfiguration;

    public FeedConfigurationResponseProcessor(FeedConfiguration feedConfiguration) {
        this.feedConfiguration = feedConfiguration;
    }

    @Override
    public void process(RequestContext rc, AdapterResponse<Feed> adapterResponse) {
        final Feed feed = adapterResponse.getBody();
        Author author = feedConfiguration.getAuthor();

        if (author != null) {
            String name = author.getName();
            if ((name != null) && !(name.isEmpty()) && (feed.getAuthor() == null)) {
                feed.addAuthor(name);
            }
        }
    }
}
