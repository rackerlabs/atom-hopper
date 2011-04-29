package net.jps.atom.hopper.abdera.filter;

import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.FeedSourceAdapterResponse;
import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class FeedPagingProcessorTest {

    public static class WhenProcessingFeedWithMoreThanOneEntry extends TestParent {

        int TOTAL_FEED_ENTRIES = 5;

        @Test
        public void shouldAddCurrentLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();

            assertThat("Should set current link", feed.getLink(REL_CURRENT), notNullValue());
            assertThat("Should match self", feed.getLink(REL_CURRENT).getHref().toString(), equalTo(SELF_URL));
        }

        @Test
        public void shouldAddNextLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            String lastEntryId = Integer.toString(TOTAL_FEED_ENTRIES);

            assertThat("Should set next link", feed.getLink(REL_NEXT), notNullValue());
            assertThat("Should reference last entry on feed", feed.getLink(REL_NEXT).getHref().toString(), equalTo(SELF_URL + "?marker=" + lastEntryId));
        }

        @Test
        public void shouldAddPrevLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            String firstEntryId = "1";

            assertThat("Should set prev link", feed.getLink(REL_PREV), notNullValue());
            assertThat("Should reference first entry on feed", feed.getLink(REL_PREV).getHref().toString(), equalTo(SELF_URL + "?marker=" + firstEntryId));
        }

    }


    public static class WhenProcessingFeedWithOneEntry extends TestParent {
        int TOTAL_FEED_ENTRIES = 1;                               

        @Test
        public void shouldAddCurrentLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();

            assertThat("Should set current link", feed.getLink(REL_CURRENT), notNullValue());
            assertThat("Should match self", feed.getLink(REL_CURRENT).getHref().toString(), equalTo(SELF_URL));
        }

        @Test
        public void shouldAddNextLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            String lastEntryId = Integer.toString(TOTAL_FEED_ENTRIES);

            assertThat("Should set next link", feed.getLink(REL_NEXT), notNullValue());
            assertThat("Should reference last entry on feed", feed.getLink(REL_NEXT).getHref().toString(), equalTo(SELF_URL + "?marker=" + lastEntryId));
        }

        @Test
        public void shouldAddPrevLink() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            String firstEntryId = "1";

            assertThat("Should set prev link", feed.getLink(REL_PREV), notNullValue());
            assertThat("Should reference first entry on feed", feed.getLink(REL_PREV).getHref().toString(), equalTo(SELF_URL + "?marker=" + firstEntryId));
        }
    }

    public static class WhenProcessingEmptyFeed extends TestParent {
        int TOTAL_FEED_ENTRIES = 0;

        @Test
        public void shouldNotAddMarkers() {
            final FeedPagingProcessor target = feedPagingProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            Feed feed = feedResponse.getBody().getAsFeed();
            assertThat("Should not set current link", feed.getLink(REL_CURRENT), nullValue());
            assertThat("Should not set prev link", feed.getLink(REL_PREV), nullValue());
            assertThat("Should not set next link", feed.getLink(REL_NEXT), nullValue());
        }
    }

    @Ignore
    public static class TestParent {

        static final String BASE_URI = "http://localhost:8080/atom";
        static final String TARGET_PATH = "/foo/bar";
        static final String SELF_URL = BASE_URI + TARGET_PATH;
        static final String REL_CURRENT = "current";
        static final String REL_NEXT = "next";
        static final String REL_PREV = "prev";


        public FeedPagingProcessor feedPagingProcessor() {
            final FeedPagingProcessor target = new FeedPagingProcessor();
            return target;
        }

        public AdapterResponse<Feed> adapterResponse(int entriesOnFeed) {
            final Feed feed = Abdera.getInstance().newFeed();

            for (int i=1; i <= entriesOnFeed; i++) {
                Entry entry = Abdera.getInstance().newEntry();
                entry.setId(Integer.toString(i));
                feed.addEntry(entry);
            }

            return new FeedSourceAdapterResponse<Feed>(feed, HttpStatus.OK, "");
        }

        public RequestContext requestContext() {
            RequestContext target = mock(RequestContext.class);

            when(target.getBaseUri()).thenReturn(new IRI(BASE_URI));
            when(target.getTargetPath()).thenReturn(TARGET_PATH);

            return target;
        }
    }
}
