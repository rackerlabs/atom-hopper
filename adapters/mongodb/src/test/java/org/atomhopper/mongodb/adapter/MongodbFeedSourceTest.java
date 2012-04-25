package org.atomhopper.mongodb.adapter;

import java.util.UUID;
import static junit.framework.Assert.assertEquals;
import org.apache.abdera.Abdera;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.mongodb.domain.PersistedEntry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;

@RunWith(Enclosed.class)
public class MongodbFeedSourceTest {

    public static class WhenSourcingFeeds {

        private MongodbFeedSource mongodbFeedSource;
        private MongoTemplate mongoTemplate;
        private GetFeedRequest getFeedRequest;
        private PersistedEntry persistedEntry;
        private Abdera abdera;
        private Query query;
        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String FEED_NAME = "namespace/feed";

        @Before
        public void setUp() throws Exception {
            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(FEED_NAME);
            persistedEntry.setEntryId(MARKER_ID);

            abdera = mock(Abdera.class);
            query = mock(Query.class);
            mongodbFeedSource = new MongodbFeedSource();
            mongoTemplate = mock(MongoTemplate.class);
            mongodbFeedSource.setMongoTemplate(mongoTemplate);
            getFeedRequest = mock(GetFeedRequest.class);
            when(mongoTemplate.findOne(query, PersistedEntry.class)).thenReturn(persistedEntry);
            when(getFeedRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getFeedRequest.getPageSize()).thenReturn("25");
            when(getFeedRequest.getAbdera()).thenReturn(abdera);
        }

        @Test
        public void shouldSetMongoTemplate() throws Exception {
            MongodbFeedSource tempMongodbFeedSource = mock(MongodbFeedSource.class);
            tempMongodbFeedSource.setMongoTemplate(mongoTemplate);
            verify(tempMongodbFeedSource).setMongoTemplate(mongoTemplate);
        }

        @Test
        public void shouldNotGetFeedWithMarkerDirectionForward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn("FORWARD");
            assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND, mongodbFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldNotGetFeedWithMarkerDirectionBackward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn("BACKWARD");
            assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND, mongodbFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        @Ignore
        public void shouldGetFeedWithMarker() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            assertEquals("Should get a 200 response", HttpStatus.OK, mongodbFeedSource.getFeed(getFeedRequest).getResponseStatus());

        }

        @Test
        public void shouldReturnBadRequestWhenMarkerUsed() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn("");
            assertEquals("Should return HTTP 400 (Bad Request)", HttpStatus.BAD_REQUEST, mongodbFeedSource.getFeed(getFeedRequest).getResponseStatus());

        }

        @Test
        @Ignore
        public void shouldReturnBadRequest() throws Exception {
            assertEquals("Should return HTTP 400 (Bad Request)", HttpStatus.BAD_REQUEST, mongodbFeedSource.getFeed(getFeedRequest).getResponseStatus());

        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldGetFeedInformation() throws Exception {
            mongodbFeedSource.getFeedInformation();
        }
    }
}