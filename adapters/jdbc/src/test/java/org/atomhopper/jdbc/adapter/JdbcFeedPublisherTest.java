package org.atomhopper.jdbc.adapter;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;

import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.jdbc.model.PersistedEntry;
import org.atomhopper.jdbc.query.PostgreSQLTextArray;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(Enclosed.class)
public class JdbcFeedPublisherTest {

    public static class WhenPostingEntries {

        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;
        private JdbcFeedPublisher jdbcFeedPublisher;
        private PostEntryRequest postEntryRequest;
        private JdbcTemplate jdbcTemplate;

        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";
        private PersistedEntry persistedEntry;
        private List<PersistedEntry> entryList;

        @Before
        public void setUp() throws Exception {
            putEntryRequest = mock(PutEntryRequest.class);
            deleteEntryRequest = mock(DeleteEntryRequest.class);
            jdbcTemplate = mock(JdbcTemplate.class);
            jdbcFeedPublisher = new JdbcFeedPublisher();
            jdbcFeedPublisher.setJdbcTemplate(jdbcTemplate);
            postEntryRequest = mock(PostEntryRequest.class);
            when(postEntryRequest.getEntry()).thenReturn(entry());
            when(postEntryRequest.getFeedName()).thenReturn("namespace/feed");

            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(FEED_NAME);
            persistedEntry.setEntryId(MARKER_ID);
            persistedEntry.setEntryBody(ENTRY_BODY);

            entryList = new ArrayList<PersistedEntry>();
            entryList.add(persistedEntry);
        }


        @Test( expected = IllegalArgumentException.class )
        public void shouldThrowExceptionForPrefixColumnMap() throws Exception {

            jdbcFeedPublisher.setDelimiter( ":" );
            jdbcFeedPublisher.afterPropertiesSet();
        }

        @Test( expected = IllegalArgumentException.class )
        public void shouldThrowExceptionForDelimiter() throws Exception {

            Map<String, String> map = new HashMap<String, String>();
            map.put( "test1", "testA" );

            jdbcFeedPublisher.setPrefixColumnMap( map );
            jdbcFeedPublisher.afterPropertiesSet();
        }

        @Test
        public void shouldReturnHTTPCreated() throws Exception {
            AdapterResponse<Entry> adapterResponse = jdbcFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test
        public void shouldThrowErrorForEntryIdAlreadyExists() throws Exception {
            jdbcFeedPublisher.setAllowOverrideId(true);
            // there are now 2 flavors of jdbcTemplate.update()
            when(jdbcTemplate.update(anyString(), new Object[]{
                    anyString(), anyString(), anyString(), any(PostgreSQLTextArray.class)
            })).thenThrow(new DuplicateKeyException("duplicate entry"));
            when(jdbcTemplate.update(anyString(), new Object[]{
                    anyString(), any(java.util.Date.class), any(java.util.Date.class),
                    anyString(), anyString(), any(PostgreSQLTextArray.class)
            })).thenThrow(new DuplicateKeyException("duplicate entry"));
            AdapterResponse<Entry> adapterResponse = jdbcFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 409 (Conflict)", HttpStatus.CONFLICT, adapterResponse.getResponseStatus());
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldPutEntry() throws Exception {
            jdbcFeedPublisher.putEntry(putEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldDeleteEntry() throws Exception {
            jdbcFeedPublisher.deleteEntry(deleteEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            jdbcFeedPublisher.setParameters(map);
        }

        public Entry entry() {
            final FOMEntry entry = new FOMEntry();
            entry.setId(UUID.randomUUID().toString());
            entry.setContent("testing");
            entry.addCategory("category");
            return entry;
        }
    }
}
