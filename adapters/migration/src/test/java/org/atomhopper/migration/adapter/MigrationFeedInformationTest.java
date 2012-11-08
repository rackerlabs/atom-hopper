package org.atomhopper.migration.adapter;

import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;

import org.atomhopper.migration.domain.MigrationReadFrom;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class MigrationFeedInformationTest {
    public static class WhenGettingMigrationFeedInformation {

        private FeedRequest feedRequest;
        private GetCategoriesRequest getCategoriesRequest;
        private MigrationFeedInformation migrationFeedInformation;

        private FeedInformation oldFeedInformation;
        private FeedInformation newFeedInformation;

        @Before
        public void setUp() throws Exception {
            feedRequest = mock(FeedRequest.class);
            getCategoriesRequest = mock(GetCategoriesRequest.class);

            oldFeedInformation = mock(FeedInformation.class);
            newFeedInformation = mock(FeedInformation.class);

            migrationFeedInformation = new MigrationFeedInformation();
            migrationFeedInformation.setNewFeedInformation(newFeedInformation);
            migrationFeedInformation.setOldFeedInformation(oldFeedInformation);
            migrationFeedInformation.setReadFrom(MigrationReadFrom.OLD);
        }

        @Test
        public void shouldCreatePostgresFeedInformation() throws Exception {
            assertNotNull(migrationFeedInformation);
        }

        @Test
        public void shouldReturnIdFromOld() throws Exception {
            when(oldFeedInformation.getId(feedRequest)).thenReturn("");
            migrationFeedInformation.getId(feedRequest);
        }

        @Test
        public void shouldReturnIdFromNew() throws Exception {
            migrationFeedInformation.setReadFrom(MigrationReadFrom.NEW);
            when(newFeedInformation.getId(feedRequest)).thenReturn("");
            migrationFeedInformation.getId(feedRequest);
        }

        @Test(expected=UnsupportedOperationException.class)
        public void shouldReturnCategories() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            migrationFeedInformation.getCategories(getCategoriesRequest);
        }
    }
}
