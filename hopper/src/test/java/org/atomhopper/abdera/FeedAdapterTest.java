package org.atomhopper.abdera;

import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.abdera.FeedAdapter;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.request.*;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.response.FeedSourceAdapterResponse;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.stax.FOMEntry;
import org.apache.abdera.parser.stax.FOMFeed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class FeedAdapterTest {

    public static class WhenPostingEntryToFeed extends TestParent {

        @Test
        public void shouldReturnUnsupportedMethodGivenNoFeedPublisher() {
            FeedAdapter feedAdapter = feedAdapter(false);
            ResponseContext responseContext = feedAdapter.postEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with " + STATUS_CODE_UNSUPPORTED_METHOD, STATUS_CODE_UNSUPPORTED_METHOD, responseContext.getStatus());
        }

        @Test
        public void shouldReturnEntryResponse() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedPublisher.postEntry(any(PostEntryRequest.class))).thenReturn(adapterResponseForEntry());
            ResponseContext responseContext = feedAdapter.postEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 200", 200, responseContext.getStatus());
        }

        @Test
        public void shouldReturnServerErrorOnFeedPublisherException() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedPublisher.postEntry(any(PostEntryRequest.class))).thenThrow(new RuntimeException());
            ResponseContext responseContext = feedAdapter.postEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 500", 500, responseContext.getStatus());
        }
    }

    public static class WhenPuttingEntryToFeed extends TestParent {

        @Test
        public void shouldReturnUnsupportedMethodGivenNoFeedPublisher() {
            FeedAdapter feedAdapter = feedAdapter(false);
            ResponseContext responseContext = feedAdapter.putEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with " + STATUS_CODE_UNSUPPORTED_METHOD, STATUS_CODE_UNSUPPORTED_METHOD, responseContext.getStatus());
        }

        @Test
        public void shouldReturnEntryResponse() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedPublisher.putEntry(any(PutEntryRequest.class))).thenReturn(adapterResponseForEntry());
            ResponseContext responseContext = feedAdapter.putEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 200", 200, responseContext.getStatus());
        }

        @Test
        public void shouldReturnServerErrorOnFeedPublisherException() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedPublisher.putEntry(any(PutEntryRequest.class))).thenThrow(new RuntimeException());
            ResponseContext responseContext = feedAdapter.putEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 500", 500, responseContext.getStatus());
        }
    }

    public static class WhenDeletingEntryFromFeed extends TestParent {

        @Test
        public void shouldReturnUnsupportedMethodGivenNoFeedPublisher() {
            FeedAdapter feedAdapter = feedAdapter(false);
            ResponseContext responseContext = feedAdapter.deleteEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with " + STATUS_CODE_UNSUPPORTED_METHOD, STATUS_CODE_UNSUPPORTED_METHOD, responseContext.getStatus());
        }

        @Test
        public void shouldReturnEntryResponse() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedPublisher.deleteEntry(any(DeleteEntryRequest.class))).thenReturn(adapterResponseForEmptyBody(HttpStatus.OK));
            ResponseContext responseContext = feedAdapter.deleteEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 204", 204, responseContext.getStatus());
        }

        @Test
        public void shouldReturnServerErrorOnFeedPublisherException() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedPublisher.deleteEntry(any(DeleteEntryRequest.class))).thenThrow(new RuntimeException());
            ResponseContext responseContext = feedAdapter.deleteEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 500", 500, responseContext.getStatus());
        }
    }

    public static class WhenGettingEntryFromFeed extends TestParent {

        @Test
        public void shouldReturnEntryResponse() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedSource.getEntry(any(GetEntryRequest.class))).thenReturn(adapterResponseForEntry());
            ResponseContext responseContext = feedAdapter.getEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 200", 200, responseContext.getStatus());
        }

        @Test
        public void shouldReturnServerErrorOnFeedSourceException() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedSource.getEntry(any(GetEntryRequest.class))).thenThrow(new RuntimeException());
            ResponseContext responseContext = feedAdapter.getEntry(REQUEST_CONTEXT);
            assertEquals("Should respond with 500", 500, responseContext.getStatus());
        }
    }

    public static class WhenGettingFeed extends TestParent {

        @Test
        public void shouldReturn200Response() {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedSource.getFeed(any(GetFeedRequest.class))).thenReturn(adapterResponseForFeed());
            ResponseContext responseContext = feedAdapter.getFeed(REQUEST_CONTEXT);
            assertEquals("Should respond with 200", 200, responseContext.getStatus());
        }

        @Test
        public void shouldReturnServerErrorOnFeedSourceException() throws IOException {
            FeedAdapter feedAdapter = feedAdapter(true);
            when(feedSource.getFeed(any(GetFeedRequest.class))).thenThrow(new RuntimeException());
            ResponseContext responseContext = feedAdapter.getFeed(REQUEST_CONTEXT);
        }
    }

    @Ignore
    public static class TestParent {

        static final int STATUS_CODE_UNSUPPORTED_METHOD = 405;
        static final String BASE_URI = "http://localhost:8080/atom/";
        static final String TARGET_PATH = "/foo/bar";
        static final String SELF = "http://localhost:8080/atom/foo/bar";
        final RequestContext REQUEST_CONTEXT = requestContext();
        FeedConfiguration feedConfiguration;
        FeedSource feedSource;
        FeedPublisher feedPublisher;

        public FeedAdapter feedAdapter(boolean supportsPublishing) {
            feedConfiguration = mock(FeedConfiguration.class);
            feedSource = mock(FeedSource.class);
            if (supportsPublishing) {
                feedPublisher = mock(FeedPublisher.class);
            } else {
                feedPublisher = null;
            }

            final FeedAdapter target = new FeedAdapter("foo", feedConfiguration, feedSource, feedPublisher);

            return target;
        }

        public Entry entry() {
            final FOMEntry entry = new FOMEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setContent("testing");
            return entry;
        }

        public Feed feed() {
            final FOMFeed feed = new FOMFeed();
            for (int i = 0; i < 5; i++) {
                feed.addEntry(entry());
            }
            return feed;
        }

        public AdapterResponse<Entry> adapterResponseForEntry() {
            return new FeedSourceAdapterResponse<Entry>(entry());
        }

        public AdapterResponse<Feed> adapterResponseForFeed() {
            return new FeedSourceAdapterResponse<Feed>(feed());
        }

        public AdapterResponse<EmptyBody> adapterResponseForEmptyBody(HttpStatus status) {
            return new FeedSourceAdapterResponse<EmptyBody>(EmptyBody.getInstance(), status, null);
        }

        public RequestContext requestContext() {
            final RequestContext context = mock(RequestContext.class);
            final Target targetMock = mock(Target.class);

            when(targetMock.getParameter(anyString())).thenReturn("");

            when(context.getResolvedUri()).thenReturn(new IRI(SELF));
            when(context.getBaseUri()).thenReturn(new IRI(BASE_URI));
            when(context.getTarget()).thenReturn(targetMock);
            when(context.getTargetPath()).thenReturn(TARGET_PATH);
            when(context.getParameterNames()).thenReturn(new String[]{});

            Document document = mock(Document.class);

            try {
                when(context.getDocument()).thenReturn(document);
            } catch (IOException e) {
                fail("Unexpected exception in test");
            }
            when(document.getRoot()).thenReturn(entry());

            return context;
        }
    }
}
