package net.jps.atom.hopper.abdera.filter;

import net.jps.atom.hopper.config.v1_0.Author;
import net.jps.atom.hopper.config.v1_0.FeedConfiguration;
import net.jps.atom.hopper.response.AdapterResponse;
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
