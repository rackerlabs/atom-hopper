package org.atomhopper.migration.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;

import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.jpa.PersistedEntry;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.migration.domain.MigrationReadFrom;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.*;

@RunWith(Enclosed.class)
public class MigrationFeedSourceTest {
    public static class WhenSourcingFeeds {

        private MigrationFeedSource migrationFeedSource;

        private FeedSource oldFeedSource;
        private FeedSource newFeedSource;

        private FeedInformation oldFeedInformation;
        private FeedInformation newFeedInformation;

        private PersistedFeed feed;

        private GetFeedRequest getFeedRequest;
        private GetEntryRequest getEntryRequest;
        private PersistedEntry persistedEntry;
        private List<PersistedEntry> entryList;
        private Abdera abdera;
        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";

        private AdapterResponse<Feed> feedResponse;
        private AdapterResponse<Entry> entryResponse;

        @Before
        public void setUp() throws Exception {
            feed = new PersistedFeed();
            feed.setName(FEED_NAME);

            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(feed);
            persistedEntry.setEntryId(MARKER_ID);
            persistedEntry.setEntryBody(ENTRY_BODY);

            entryList = new ArrayList<PersistedEntry>();
            entryList.add(persistedEntry);

            // Mocks
            abdera = mock(Abdera.class);
            getFeedRequest = mock(GetFeedRequest.class);
            getEntryRequest = mock(GetEntryRequest.class);

            oldFeedSource = mock(FeedSource.class);
            newFeedSource = mock(FeedSource.class);

            oldFeedInformation = mock(FeedInformation.class);
            newFeedInformation = mock(FeedInformation.class);

            migrationFeedSource = new MigrationFeedSource();
            migrationFeedSource.setNewFeedSource(newFeedSource);
            migrationFeedSource.setOldFeedSource(oldFeedSource);

            feedResponse = mock(AdapterResponse.class);


            // Mock GetEntryRequest
            when(getEntryRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getEntryRequest.getEntryId()).thenReturn(MARKER_ID);

            //Mock GetFeedRequest
            when(getFeedRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getFeedRequest.getPageSize()).thenReturn("25");
            when(getFeedRequest.getAbdera()).thenReturn(abdera);
        }

        @Test
        public void shouldGetFeedInformationForReadFromOld() throws Exception {
            migrationFeedSource.setReadFrom(MigrationReadFrom.OLD);
            when(oldFeedSource.getFeedInformation()).thenReturn(oldFeedInformation);
            FeedInformation info = migrationFeedSource.getFeedInformation();
            assertEquals(info, oldFeedInformation);
        }

        @Test
        public void shouldGetFeedInformationForReadFromNew() throws Exception {
            migrationFeedSource.setReadFrom(MigrationReadFrom.NEW);
            when(newFeedSource.getFeedInformation()).thenReturn(newFeedInformation);
            FeedInformation info = migrationFeedSource.getFeedInformation();
            assertEquals(info, newFeedInformation);
        }

        @Test
        public void ShouldGetFeedForReadFromOld() throws Exception {
            migrationFeedSource.setReadFrom(MigrationReadFrom.OLD);
            when(oldFeedSource.getFeed(getFeedRequest)).thenReturn(feedResponse);
            AdapterResponse<Feed> feedRead = migrationFeedSource.getFeed(getFeedRequest);
        }

        @Test
        public void ShouldGetFeedForReadFromNew() throws Exception {
            migrationFeedSource.setReadFrom(MigrationReadFrom.NEW);
            when(newFeedSource.getFeed(getFeedRequest)).thenReturn(feedResponse);
            AdapterResponse<Feed> feedRead = migrationFeedSource.getFeed(getFeedRequest);
        }

        @Test
        public void ShouldGetEntryForReadFromOld() throws Exception {
            migrationFeedSource.setReadFrom(MigrationReadFrom.OLD);
            when(oldFeedSource.getEntry(getEntryRequest)).thenReturn(entryResponse);
            AdapterResponse<Entry> entryRead = migrationFeedSource.getEntry(getEntryRequest);
        }

        @Test
        public void ShouldGetEntryForReadFromNew() throws Exception {
            migrationFeedSource.setReadFrom(MigrationReadFrom.NEW);
            when(newFeedSource.getEntry(getEntryRequest)).thenReturn(entryResponse);
            AdapterResponse<Entry> entryRead = migrationFeedSource.getEntry(getEntryRequest);
        }

        @Test(expected=UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            migrationFeedSource.setParameters(map);
        }
    }
}
