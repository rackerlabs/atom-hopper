package org.atomhopper.hibernate.adapter;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import org.apache.abdera.Abdera;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.hibernate.query.CategoryCriteriaGenerator;
import org.atomhopper.hibernate.query.SimpleCategoryCriteriaGenerator;
import org.hibernate.Criteria;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import org.springframework.http.HttpStatus;


@RunWith(Enclosed.class)
public class HibernateFeedSourceTest {

    public static class WhenCallingNonImplementedFunctionality {

        private HibernateFeedSource hibernateFeedSource;
        private FeedRepository feedRepository;
        private GetEntryRequest getEntryRequest;
        private GetFeedRequest getFeedRequest;
        private PersistedFeed persistedFeed;
        private PersistedEntry persistedEntry;

        private final String ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";
        private final String MOCK_LAST_MARKER = "last";
        private final int PAGE_SIZE = 10;
        private final String BACKWARD = "backward";

        @Before
        public void setUp() throws Exception {
            hibernateFeedSource = new HibernateFeedSource();

            // Mocks
            getEntryRequest = mock(GetEntryRequest.class);
            feedRepository = mock(FeedRepository.class);
            persistedFeed = mock(PersistedFeed.class);
            getFeedRequest = mock( GetFeedRequest.class );

            // Mock GetEntryRequest
            when(getEntryRequest.getFeedName()).thenReturn(FEED_NAME);
            when(getEntryRequest.getEntryId()).thenReturn(ID);

            // Mock PersistedFeed
            when(persistedFeed.getName()).thenReturn(FEED_NAME);

            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(persistedFeed);
            persistedEntry.setEntryId(ID);
            persistedEntry.setEntryBody(ENTRY_BODY);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            hibernateFeedSource.setParameters(map);
        }

        @Test
        public void shouldSetFeedRepository() throws Exception {
            HibernateFeedSource tempHibernateFeedSource = mock(HibernateFeedSource.class);
            tempHibernateFeedSource.setFeedRepository(feedRepository);
            verify(tempHibernateFeedSource).setFeedRepository(feedRepository);
        }

        @Test
        public void shouldNotGetEntry() throws Exception {
            when(feedRepository.getEntry(any(String.class), any(String.class))).thenReturn(null);
            hibernateFeedSource.setFeedRepository(feedRepository);
            assertEquals("Should get a 404 response", HttpStatus.NOT_FOUND, hibernateFeedSource.getEntry(getEntryRequest).getResponseStatus());

        }

        @Test
        public void shouldGetEntry() throws Exception {
            Abdera localAbdera = new Abdera();
            when(feedRepository.getEntry(ID, FEED_NAME)).thenReturn(persistedEntry);
            hibernateFeedSource.setFeedRepository(feedRepository);
            when(getEntryRequest.getAbdera()).thenReturn(localAbdera);
            assertEquals("Should get a 200 response", HttpStatus.OK, hibernateFeedSource.getEntry(getEntryRequest).getResponseStatus());

        }

        @Test
        public void shouldGetFeedWithLastMarker() throws Exception {

            Abdera localAbdera = new Abdera();
            when( getFeedRequest.getPageMarker() ).thenReturn( MOCK_LAST_MARKER );
            when( getFeedRequest.getDirection() ).thenReturn( BACKWARD );
            when( getFeedRequest.getFeedName() ).thenReturn( FEED_NAME );
            when( getFeedRequest.getAbdera() ).thenReturn( localAbdera );
            when( feedRepository.getEntry( MOCK_LAST_MARKER, FEED_NAME ) ).thenReturn( persistedEntry );
            when( feedRepository.getFeedPage( FEED_NAME, persistedEntry, PageDirection.BACKWARD,
                                              new SimpleCategoryCriteriaGenerator( "" ),
                                              PAGE_SIZE ) ).thenReturn( new ArrayList<PersistedEntry>() );

            hibernateFeedSource.setFeedRepository( feedRepository );
            assertEquals( "Should get a 200 response with marker of \"last\"", HttpStatus.OK,
                          hibernateFeedSource.getFeed( getFeedRequest ).getResponseStatus() );
        }
    }
}