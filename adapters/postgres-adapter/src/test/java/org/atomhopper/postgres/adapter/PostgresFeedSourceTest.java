package org.atomhopper.postgres.adapter;

import java.net.URL;
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
import org.atomhopper.postgres.model.PersistedEntry;
import org.atomhopper.postgres.query.EntryRowMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

@RunWith(Enclosed.class)
public class PostgresFeedSourceTest {

    public static class WhenSourcingFeeds {

        private PostgresFeedSource postgresFeedSource;
        private JdbcTemplate jdbcTemplate;
        private GetFeedRequest getFeedRequest;
        private GetEntryRequest getEntryRequest;
        private PersistedEntry persistedEntry;
        private List<PersistedEntry> entryList;
        private List<PersistedEntry> emptyList;
        private Abdera abdera;
        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";
        private final String COLLECTION_NAME = "namespace.feed";
        private final String FORWARD = "forward";
        private final String BACKWARD = "backward";
        private final String SINGLE_CAT = "+Cat1";
        private final String MULTI_CAT = "+Cat1+Cat2";
        private final String MOCK_LAST_MARKER = "last";
        private final String CURRENT = "current";

        @Before
        public void setUp() throws Exception {
            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(FEED_NAME);
            persistedEntry.setEntryId(MARKER_ID);
            persistedEntry.setEntryBody(ENTRY_BODY);

            emptyList = new ArrayList<PersistedEntry>();

            entryList = new ArrayList<PersistedEntry>();
            entryList.add(persistedEntry);

            // Mocks
            abdera = mock(Abdera.class);
            getFeedRequest = mock(GetFeedRequest.class);
            getEntryRequest = mock(GetEntryRequest.class);
            jdbcTemplate = mock(JdbcTemplate.class);

            postgresFeedSource = new PostgresFeedSource();
            postgresFeedSource.setJdbcTemplate( jdbcTemplate );

            // Mock GetEntryRequest
            when(getEntryRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getEntryRequest.getEntryId()).thenReturn(MARKER_ID);

            //Mock GetFeedRequest
            when(getFeedRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getFeedRequest.getPageSize()).thenReturn("25");
            when(getFeedRequest.getAbdera()).thenReturn(abdera);
        }

        @Test
        public void shouldSetJdbcTemplate() throws Exception {
            PostgresFeedSource tempPostgresFeedSource = mock(PostgresFeedSource.class);
            tempPostgresFeedSource.setJdbcTemplate(jdbcTemplate);
            verify(tempPostgresFeedSource).setJdbcTemplate(jdbcTemplate);
        }

        @Test
        public void shouldNotGetFeedWithMarkerDirectionForward() throws Exception {
            Abdera localAbdera = new Abdera();
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn("FORWARD");
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(null);
            assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldNotGetFeedWithMarkerDirectionBackward() throws Exception {
            Abdera localAbdera = new Abdera();
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn("BACKWARD");
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(null);
            assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedHead() throws Exception {
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            when(jdbcTemplate.queryForObject(eq(any(String.class)), any(Object[].class), Integer.class)).thenReturn(1);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedHeadWithCategory() throws Exception {
            Abdera localAbdera = new Abdera();
            when(getFeedRequest.getSearchQuery()).thenReturn(SINGLE_CAT);
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            when(jdbcTemplate.queryForObject(eq(any(String.class)), any(Object[].class), Integer.class)).thenReturn(1);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedWithMarkerForward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn(FORWARD);
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedWithMarkerBackward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn(BACKWARD);
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedWithCategoryWithMarkerForward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn(FORWARD);
            when(getFeedRequest.getSearchQuery()).thenReturn(SINGLE_CAT);
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedWithCategoryWithMarkerBackward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn(BACKWARD);
            when(getFeedRequest.getSearchQuery()).thenReturn(SINGLE_CAT);
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedWithCategoriesWithMarkerForward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn(FORWARD);
            when(getFeedRequest.getSearchQuery()).thenReturn(MULTI_CAT);
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldGetFeedWithCategoriesWithMarkerBackward() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn(BACKWARD);
            when(getFeedRequest.getSearchQuery()).thenReturn(MULTI_CAT);
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        public void shouldReturnBadRequestWhenMarkerUsed() throws Exception {
            when(getFeedRequest.getPageMarker()).thenReturn(MARKER_ID);
            when(getFeedRequest.getDirection()).thenReturn("");
            assertEquals("Should return HTTP 400 (Bad Request)", HttpStatus.BAD_REQUEST,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        @Ignore
        public void shouldReturnBadRequest() throws Exception {
            assertEquals("Should return HTTP 400 (Bad Request)", HttpStatus.BAD_REQUEST,
                         postgresFeedSource.getFeed(getFeedRequest).getResponseStatus());

        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldGetFeedInformation() throws Exception {
            postgresFeedSource.getFeedInformation();
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            postgresFeedSource.setParameters(map);
        }

        @Test
        public void shouldNotGetEntry() throws Exception {
            Abdera localAbdera = new Abdera();
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(emptyList);
            assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND,
                         postgresFeedSource.getEntry(getEntryRequest).getResponseStatus());

        }

        @Test
        public void shouldGetEntry() throws Exception {
            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         postgresFeedSource.getEntry(getEntryRequest).getResponseStatus());

        }

        @Test
        public void shouldGetFeedWithLastMarker() throws Exception {

            Abdera localAbdera = new Abdera();
            when( getFeedRequest.getPageMarker() ).thenReturn( MOCK_LAST_MARKER );
            when( getFeedRequest.getDirection() ).thenReturn( BACKWARD );
            when( getFeedRequest.getFeedName() ).thenReturn( FEED_NAME );
            when( getFeedRequest.getAbdera() ).thenReturn( localAbdera );
            when(jdbcTemplate.queryForObject( any( String.class ),
                                              any( EntryRowMapper.class ),
                                              any( String.class ),
                                              any( String.class ) ) ).thenReturn( persistedEntry );
            when( jdbcTemplate.query( any( String.class ),
                                      any( Object[].class ),
                                      any( EntryRowMapper.class ) ) ).thenReturn( entryList );
            postgresFeedSource.setJdbcTemplate( jdbcTemplate );
            assertEquals( "Should get a 200 response with marker of \"last\"", HttpStatus.OK,
                          postgresFeedSource.getFeed( getFeedRequest ).getResponseStatus() );

            /* TODO:  Other adapters test for the next-archive link here, but the same .query() call is used
               get the entries as well as testing to determine if the feed is empty.  I'm not sure how
               to mock this up */

        }

        @Test
        public void shouldGetCurrentLinkFromArchiveFeedAndArchiveNode() throws Exception {

            final String currentURL = "http://current.com/namespace/feed";

            PostgresFeedSource archiveSource = new PostgresFeedSource();
            archiveSource.setJdbcTemplate( jdbcTemplate );
            archiveSource.setCurrentUrl( new URL( currentURL ) );

            Abdera localAbdera = new Abdera();
            when(jdbcTemplate.queryForObject(any(String.class),
                                             any(EntryRowMapper.class),
                                             any(String.class),
                                             any(String.class))).thenReturn(persistedEntry);
            when(getFeedRequest.getAbdera()).thenReturn(localAbdera);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(entryList);
            when(jdbcTemplate.queryForObject(eq(any(String.class)), any(Object[].class), Integer.class)).thenReturn(1);
//            when(jdbcTemplate.queryForInt(any(String.class), any(Object[].class))).thenReturn(1);
            assertEquals("Should get a 200 response", HttpStatus.OK,
                         archiveSource.getFeed(getFeedRequest).getResponseStatus());


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
