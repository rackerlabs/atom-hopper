package net.jps.atom.hopper.abdera.response;

import net.jps.atom.hopper.config.v1_0.FeedConfiguration;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.FeedSourceAdapterResponse;
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
public class StaticFeedResponseHandlerTest {

    public static class WhenHandlingAdapterResponse extends TestParent {

        @Test
        public void shouldReturnWith304Response() {

            StaticFeedResponseHandler responseHandler = responseHandler();
            RequestContext requestContext = requestContext();

            AdapterResponse<Feed> adapterResponse = adapterResponseForFeed(2);

            EntityTag requestEntityTag = new EntityTag(adapterResponse.getBody().getEntries().get(0).getId().toString(), true);
            when(requestContext.getIfNoneMatch()).thenReturn(new EntityTag[] {requestEntityTag});

            ResponseContext responseContext = responseHandler.handleAdapterResponse(requestContext, adapterResponse);
            assertEquals("Should respond with 304", 304, responseContext.getStatus());
        }
    }


    @Ignore
    public static class TestParent {

        FeedConfiguration feedConfiguration;

        static final String BASE_URI = "http://localhost:8080/atom/";
        static final String TARGET_PATH = "/foo/bar";
        static final String SELF = "http://localhost:8080/atom/foo/bar";

        public StaticFeedResponseHandler responseHandler() {
            feedConfiguration = mock(FeedConfiguration.class);
            final StaticFeedResponseHandler target = new StaticFeedResponseHandler(feedConfiguration);
            return target;
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
