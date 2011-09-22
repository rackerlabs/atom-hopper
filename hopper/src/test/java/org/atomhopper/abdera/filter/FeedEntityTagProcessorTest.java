package org.atomhopper.abdera.filter;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.FeedSourceAdapterResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class FeedEntityTagProcessorTest {

    public static class WhenProcessingFeedWithZeroEntries extends TestParent {

        @Test
        public void shouldNotSetEtag() {
            FeedEntityTagProcessor target = etagHeaderProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(0);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            assertThat("Should not set etag header", feedResponse.getEntityTag(), nullValue());
        }

    }

    public static class WhenProcessingFeedWithMoreThanOneEntry extends TestParent {

        final int TOTAL_FEED_ENTRIES = 5;

        @Test
        public void shouldSetEtagHeaderToFirstEntry() {
            FeedEntityTagProcessor target = etagHeaderProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            String expectedEtag = feedResponse.getBody().getEntries().get(0).getId().toString() + ":" + feedResponse.getBody().getEntries().get(4).getId().toString();

            assertThat("Should set etag", feedResponse.getEntityTag(), notNullValue());
            assertThat("Should set etag value to first entry id : last entry id", feedResponse.getEntityTag().getTag(), equalTo(expectedEtag));
            assertThat("Should set weak etag", feedResponse.getEntityTag().isWeak(), equalTo(true));
        }
    }

    public static class WhenProcessingFeedWithOneEntry extends TestParent {

        final int TOTAL_FEED_ENTRIES = 1;

        @Test
        public void shouldSetEtagHeaderToFirstEntry() {
            FeedEntityTagProcessor target = etagHeaderProcessor();
            final AdapterResponse<Feed> feedResponse = adapterResponse(TOTAL_FEED_ENTRIES);
            final RequestContext rc = requestContext();

            target.process(rc, feedResponse);

            String expectedEtag = feedResponse.getBody().getEntries().get(0).getId().toString() + ":" + feedResponse.getBody().getEntries().get(0).getId().toString();

            assertThat("Should set etag", feedResponse.getEntityTag(), notNullValue());
            assertThat("Should set etag value to first entry id : first entry id", feedResponse.getEntityTag().getTag(), equalTo(expectedEtag));
            assertThat("Should set weak etag", feedResponse.getEntityTag().isWeak(), equalTo(true));
        }
    }


    @Ignore
    public static class TestParent {

        public FeedEntityTagProcessor etagHeaderProcessor() {
            return new FeedEntityTagProcessor();
        }

        public AdapterResponse<Feed> adapterResponse(int entriesOnFeed) {
            final Feed feed = Abdera.getInstance().newFeed();

            for (int i = 1; i <= entriesOnFeed; i++) {
                Entry entry = Abdera.getInstance().newEntry();
                entry.setId(Integer.toString(i));
                feed.addEntry(entry);
            }

            return new FeedSourceAdapterResponse<Feed>(feed, HttpStatus.OK, "");
        }

        public RequestContext requestContext() {
            return mock(RequestContext.class);
        }
    }
}
