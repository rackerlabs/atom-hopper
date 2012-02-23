package org.atomhopper.abdera.filter;

import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Person;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.config.v1_0.Author;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.FeedSourceAdapterResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FeedConfigurationResponseProcessorTest extends TestParent {

    @Test
    public void shouldSetFeedAuthorIfSpecified() {
        feedConfiguration.setAuthor(feedAuthor);
        Feed feed = getFeedResponseAsFeed(feedConfiguration);
        Person author = feed.getAuthor();
        assertThat("author should exist", author, notNullValue());
        assertThat("author should match", feed.getAuthor().getName(), equalTo(feedAuthorName));
    }

    @Test
    public void shouldNotSetFeedAuthorIfNotSpecified() {
        Feed feed = getFeedResponseAsFeed(feedConfiguration);
        Person author = feed.getAuthor();
        assertThat("author should not exist", author, nullValue());
    }
}

@Ignore
class TestParent {

    private static final String BASE_URI = "http://localhost:8080/atom";
    private static final String TARGET_PATH = "/foo/bar";
    static final String feedAuthorName = "Feed Author";
    static final Author feedAuthor = newAuthor(feedAuthorName);
    final FeedConfiguration feedConfiguration = new FeedConfiguration();

    private static Author newAuthor(String authorName) {
        Author author = new Author();
        author.setName(authorName);
        return author;
    }

    public FeedConfigurationResponseProcessor feedDefaultsProcessor(FeedConfiguration feedConfiguration) {
        return new FeedConfigurationResponseProcessor(feedConfiguration);
    }

    public AdapterResponse<Feed> adapterResponse() {
        final Feed feed = Abdera.getInstance().newFeed();
        return new FeedSourceAdapterResponse<Feed>(feed, HttpStatus.OK, "");
    }

    public RequestContext requestContext() {
        RequestContext target = mock(RequestContext.class);

        when(target.getBaseUri()).thenReturn(new IRI(BASE_URI));
        when(target.getTargetPath()).thenReturn(TARGET_PATH);

        return target;
    }

    Feed getFeedResponseAsFeed(FeedConfiguration feedConfiguration) {
        final FeedConfigurationResponseProcessor target = feedDefaultsProcessor(feedConfiguration);
        final AdapterResponse<Feed> feedResponse = adapterResponse();
        final RequestContext rc = requestContext();
        target.process(rc, feedResponse);
        return feedResponse.getBody().getAsFeed();
    }
}
