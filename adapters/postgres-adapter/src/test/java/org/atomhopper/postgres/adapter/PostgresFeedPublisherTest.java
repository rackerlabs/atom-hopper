package org.atomhopper.postgres.adapter;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.postgres.model.PersistedEntry;
import org.atomhopper.postgres.query.EntryRowMapper;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.atomhopper.adapter.PublicationException;

@RunWith(Enclosed.class)
public class PostgresFeedPublisherTest {

    public static class WhenPostingEntries {

        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;
        private PostgresFeedPublisher postgresFeedPublisher;
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
            postgresFeedPublisher = new PostgresFeedPublisher();
            postgresFeedPublisher.setJdbcTemplate(jdbcTemplate);
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

        @Test
        public void shouldReturnHTTPCreated() throws Exception {
            AdapterResponse<Entry> adapterResponse = postgresFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test(expected = PublicationException.class)
        public void shouldThrowErrorForEntryIdAlreadyExists() throws Exception {
            postgresFeedPublisher.setAllowOverrideId(true);
            when(jdbcTemplate.query(any(String.class), any(Object[].class), any(EntryRowMapper.class))).thenReturn(
                    entryList);
            AdapterResponse<Entry> adapterResponse = postgresFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldPutEntry() throws Exception {
            postgresFeedPublisher.putEntry(putEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldDeleteEntry() throws Exception {
            postgresFeedPublisher.deleteEntry(deleteEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            postgresFeedPublisher.setParameters(map);
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
