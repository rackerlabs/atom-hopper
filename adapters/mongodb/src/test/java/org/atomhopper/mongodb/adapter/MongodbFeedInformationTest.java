package org.atomhopper.mongodb.adapter;

import org.atomhopper.adapter.request.adapter.GetCategoriesRequest;
import org.atomhopper.adapter.request.feed.FeedRequest;
import org.springframework.data.mongodb.core.MongoTemplate;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


@RunWith(Enclosed.class)
public class MongodbFeedInformationTest {

    public static class WhenGettingMongodbFeedInformation {

        private MongoTemplate mongoTemplate;
        private FeedRequest feedRequest;
        private GetCategoriesRequest getCategoriesRequest;
        private MongodbFeedInformation mongodbFeedInformation;

        @Before
        public void setUp() throws Exception {
            mongoTemplate = mock(MongoTemplate.class);
            feedRequest = mock(FeedRequest.class);
            getCategoriesRequest = mock(GetCategoriesRequest.class);

            mongodbFeedInformation = new MongodbFeedInformation(mongoTemplate);
        }

        @Test
        public void shouldCreateHibernateFeedInformation() throws Exception {
            assertNotNull(mongodbFeedInformation);
        }

        @Test(expected=UnsupportedOperationException.class)
        public void shouldReturnId() throws Exception {
            mongodbFeedInformation.getId(feedRequest);
        }

        @Test(expected=UnsupportedOperationException.class)
        public void shouldReturnCategories() throws Exception {
            mongodbFeedInformation.getCategories(getCategoriesRequest);
        }
    }
}