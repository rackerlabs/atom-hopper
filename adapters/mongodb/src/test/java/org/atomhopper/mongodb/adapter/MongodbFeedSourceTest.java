package org.atomhopper.mongodb.adapter;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;
import org.apache.abdera.Abdera;
import org.apache.abdera.i18n.iri.IRI;
import org.apache.abdera.model.Element;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.AdapterHelper;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.mongodb.domain.PersistedEntry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;

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
        private final String MOCK_LAST_MARKER = "last";
        private final String BACKWARD = "backward";
        private final String NEXT_ARCHIVE = "next-archive";
        private final String ARCHIVE_LINK = "http://archive.com/namespace/feed/archive";
        private final String CURRENT = "current";


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
            mongodbFeedSource.setArchiveUrl( new URL( ARCHIVE_LINK ) );

            // Mock MongoTemplate
            //when(mongoTemplate.findOne(query, PersistedEntry.class)).thenReturn(persistedEntry);
            when( mongoTemplate.findOne( query, PersistedEntry.class, COLLECTION_NAME ) ).thenReturn(persistedEntry);

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

        @Test
        public void shouldGetFeedWithLastMarkerAndContainNextArchive() throws Exception {

            Abdera localAbdera = new Abdera();
            when( getFeedRequest.getPageMarker() ).thenReturn( MOCK_LAST_MARKER );
            when( getFeedRequest.getDirection() ).thenReturn( BACKWARD );
            when( getFeedRequest.getFeedName() ).thenReturn( FEED_NAME );
            when( getFeedRequest.getAbdera() ).thenReturn( localAbdera );
            when( mongoTemplate.findOne(any(Query.class), any(Class.class), eq(COLLECTION_NAME))).thenReturn( persistedEntry );
            mongodbFeedSource.setMongoTemplate( mongoTemplate );
            assertEquals( "Should get a 200 response with marker of \"last\"", HttpStatus.OK,
                          mongodbFeedSource.getFeed( getFeedRequest ).getResponseStatus() );

            IRI iri = mongodbFeedSource.getFeed(getFeedRequest).getBody().getLink( NEXT_ARCHIVE ).getHref();
            assertTrue("'next-archive' link should contain \"" + ARCHIVE_LINK + "\"", iri.toString().contains( ARCHIVE_LINK ) );
        }

        @Test
        public void shouldGetCurrentLinkFromArchiveFeedAndArchiveNode() throws Exception {

            final String currentURL = "http://current.com/namespace/feed";

            MongodbFeedSource archiveSource = new MongodbFeedSource();
            mongodbFeedSource.setMongoTemplate(mongoTemplate);
            archiveSource.setCurrentUrl( new URL( currentURL ) );

            Abdera localAbdera = new Abdera();
            when( getFeedRequest.getPageMarker() ).thenReturn( MOCK_LAST_MARKER );
            when( getFeedRequest.getDirection() ).thenReturn( BACKWARD );
            when( getFeedRequest.getFeedName() ).thenReturn( FEED_NAME );
            when( getFeedRequest.getAbdera() ).thenReturn( localAbdera );
            when( mongoTemplate.findOne(any(Query.class), any(Class.class), eq(COLLECTION_NAME))).thenReturn( persistedEntry );
            archiveSource.setMongoTemplate( mongoTemplate );
            assertEquals( "Should get a 200 response with marker of \"last\"", HttpStatus.OK,
                          archiveSource.getFeed( getFeedRequest ).getResponseStatus() );

            IRI iri = archiveSource.getFeed(getFeedRequest).getBody().getLink( CURRENT ).getHref();
            assertTrue("'current' link should contain \"" + currentURL + "\"", iri.toString().contains( currentURL ) );

            Feed feed = archiveSource.getFeed( getFeedRequest ).getBody();

            boolean found = false;

            for( Element e : feed.getElements() ) {

                if ( e.getQName().getLocalPart().equals( AdapterHelper.ARCHIVE )
                      && e.getQName().getPrefix().equals( AdapterHelper.ARCHIVE_PREFIX )
                      && e.getQName().getNamespaceURI().equals( AdapterHelper.ARCHIVE_NS ) ) {

                    found = true;
                    break;
                }
            }

            assertTrue("'<fn:archive>' node should exist", found );
        }

    }
}