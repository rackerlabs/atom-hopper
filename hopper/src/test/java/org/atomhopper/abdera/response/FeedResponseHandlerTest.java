package org.atomhopper.abdera.response;

import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.parser.stax.FOMEntry;
import org.apache.abdera.parser.stax.FOMFeed;
import org.apache.abdera.protocol.server.RequestContext;
import org.apache.abdera.protocol.server.ResponseContext;
import org.apache.abdera.protocol.server.Target;
import org.apache.abdera.util.EntityTag;
import org.atomhopper.abdera.filter.FeedEntityTagProcessor;
import org.atomhopper.abdera.filter.FeedPagingProcessor;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.FeedSourceAdapterResponse;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class FeedResponseHandlerTest {

    public static class WhenHandlingAdapterResponse extends TestParent {

        @Test
        public void shouldReturnWith304Response() {

            FeedResponseHandler responseHandler = responseHandler();
            RequestContext requestContext = requestContext();

            AdapterResponse<Feed> adapterResponse = adapterResponseForFeed(2);

            String entityTagValue = adapterResponse.getBody().getEntries().get(0).getId().toString() + ":" + adapterResponse.getBody().getEntries().get(1).getId().toString();

            EntityTag requestEntityTag = new EntityTag(entityTagValue, true);
            when(requestContext.getIfNoneMatch()).thenReturn(new EntityTag[] {requestEntityTag});

            ResponseContext responseContext = responseHandler.handleResponse(requestContext, adapterResponse);
            assertEquals("Should respond with 304", 304, responseContext.getStatus());
            assertEquals("Should have ETag", requestEntityTag, responseContext.getEntityTag());
        }
    }


    @Ignore
    public static class TestParent {

        static final String BASE_URI = "http://localhost:8080/atom/";
        static final String TARGET_PATH = "/foo/bar";
        static final String SELF = "http://localhost:8080/atom/foo/bar";

        public FeedResponseHandler responseHandler() {
            return new FeedResponseHandler(new String[] {"GET"}, new FeedEntityTagProcessor(), new FeedPagingProcessor());
        }

        public AdapterResponse<Feed> adapterResponseForFeed(int entriesOnFeed) {
            return new FeedSourceAdapterResponse<Feed>(feed(entriesOnFeed));
        }

        public Feed feed(int entriesOnFeed) {
            final FOMFeed feed = new FOMFeed();
            for (int i = 0; i < entriesOnFeed; i++) {
                feed.addEntry(entry());
            }
            return feed;
        }

        public Entry entry() {
            final FOMEntry entry = new FOMEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setContent("testing");
            return entry;
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
