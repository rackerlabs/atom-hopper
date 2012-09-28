package org.atomhopper.postgres.adapter;

import org.apache.abdera.Abdera;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.postgres.model.PersistedEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(Enclosed.class)
public class PostgresFeedSourceTest {

    private PostgresFeedSource postgresFeedSource;
    private JdbcTemplate jdbcTemplate;
    private GetFeedRequest getFeedRequest;
    private GetEntryRequest getEntryRequest;
    private PersistedEntry persistedEntry;
    private Abdera abdera;
    private final String MARKER_ID = UUID.randomUUID().toString();
    private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
    private final String FEED_NAME = "namespace/feed";
    private final String COLLECTION_NAME = "namespace.feed";

    @Rule
    public ExpectedException exception = ExpectedException.none();


    @Before
    public void setUp() throws Exception {
        persistedEntry = new PersistedEntry();
        persistedEntry.setFeed(FEED_NAME);
        persistedEntry.setEntryId(MARKER_ID);
        persistedEntry.setEntryBody(ENTRY_BODY);

        // Mocks
        abdera = mock(Abdera.class);
        getFeedRequest = mock(GetFeedRequest.class);
        getEntryRequest = mock(GetEntryRequest.class);
        jdbcTemplate = mock(JdbcTemplate.class);

        postgresFeedSource = new PostgresFeedSource();
        postgresFeedSource.setJdbcTemplate(jdbcTemplate);

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
    public void setParametersShouldThrowUnsupportedError() throws Exception {
        exception.expect(UnsupportedOperationException.class);
        postgresFeedSource.setParameters(new HashMap<String, String>());
    }
}
