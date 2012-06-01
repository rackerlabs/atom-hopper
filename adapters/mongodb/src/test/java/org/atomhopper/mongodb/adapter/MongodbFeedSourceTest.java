package org.atomhopper.mongodb.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;
import org.apache.abdera.Abdera;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
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
import org.apache.abdera.parser.Parser;

@RunWith(Enclosed.class)
public class MongodbFeedSourceTest {

    public static class WhenSourcingFeeds {

        private MongodbFeedSource mongodbFeedSource;
        private MongoTemplate mongoTemplate;
        private GetFeedRequest getFeedRequest;
        private GetEntryRequest getEntryRequest;
        private PersistedEntry persistedEntry;
        private Abdera abdera;
        private Query query;
        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";
        private final String COLLECTION_NAME = "namespace.feed";

        @Before
        public void setUp() throws Exception {
            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(FEED_NAME);
            persistedEntry.setEntryId(MARKER_ID);
            persistedEntry.setEntryBody(ENTRY_BODY);

            // Mocks
            abdera = mock(Abdera.class);
            query = mock(Query.class);
            getFeedRequest = mock(GetFeedRequest.class);
            getEntryRequest = mock(GetEntryRequest.class);
            mongoTemplate = mock(MongoTemplate.class);

            mongodbFeedSource = new MongodbFeedSource();
            mongodbFeedSource.setMongoTemplate(mongoTemplate);

            // Mock MongoTemplate
            //when(mongoTemplate.findOne(query, PersistedEntry.class)).thenReturn(persistedEntry);
            when(mongoTemplate.findOne(query, PersistedEntry.class, COLLECTION_NAME)).thenReturn(persistedEntry);

            // Mock GetEntryRequest
            when(getEntryRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getEntryRequest.getEntryId()).thenReturn(MARKER_ID);
            //when(getEntryRequest.getAbdera()).thenReturn(abdera);

            //Mock GetFeedRequest
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

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            mongodbFeedSource.setParameters(map);
        }

        @Test
        public void shouldNotGetEntry() throws Exception {
            when(mongoTemplate.findOne(any(Query.class), any(Class.class), eq(COLLECTION_NAME))).thenReturn(null);
            assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND, mongodbFeedSource.getEntry(getEntryRequest).getResponseStatus());

        }

        @Test
        public void shouldGetEntry() throws Exception {
            Abdera localAbdera = new Abdera();
            when(mongoTemplate.findOne(any(Query.class), any(Class.class), eq(COLLECTION_NAME))).thenReturn(persistedEntry);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            assertEquals("Should get a 200 response", HttpStatus.OK, mongodbFeedSource.getEntry(getEntryRequest).getResponseStatus());

        }
    }
}