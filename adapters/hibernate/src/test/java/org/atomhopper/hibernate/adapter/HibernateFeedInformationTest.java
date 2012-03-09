package org.atomhopper.hibernate.adapter;

import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;
import org.atomhopper.dbal.FeedRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: sbrayman
 * Date: Sep 26, 2011
 */

@RunWith(Enclosed.class)
public class HibernateFeedInformationTest {

    public static class WhenGettingHibernateFeedInformation {

        private FeedRepository feedRepository;
        private FeedRequest feedRequest;
        private PersistedFeed persistedFeed;
        private GetCategoriesRequest getCategoriesRequest;
        private HibernateFeedInformation hibernateFeedInformation;
        private static final String id = "urn:uuid:c0331543-662d-4ac2-9a2a-95381812d6b6";

        @Before
        public void setUp() throws Exception {
            feedRepository = mock(FeedRepository.class);
            feedRequest = mock(FeedRequest.class);
            getCategoriesRequest = mock(GetCategoriesRequest.class);
            persistedFeed = mock(PersistedFeed.class);
            
            when(feedRepository.getFeed("feedname")).thenReturn(persistedFeed);
            when(feedRequest.getFeedName()).thenReturn("feedname");
            when(persistedFeed.getFeedId()).thenReturn(id);
            
            hibernateFeedInformation = new HibernateFeedInformation(feedRepository);
        }

        @Test
        public void shouldCreateHibernateFeedInformation() throws Exception {
            assertNotNull(hibernateFeedInformation);
        }

        @Test
        public void shouldReturnId() throws Exception {
            assertEquals("Should return the correct id", 
                    hibernateFeedInformation.getId(feedRequest), id);
        } 

        @Test(expected=UnsupportedOperationException.class)
        public void shouldReturnCategories() throws Exception {
            hibernateFeedInformation.getCategories(getCategoriesRequest);
        }        
    }
}
