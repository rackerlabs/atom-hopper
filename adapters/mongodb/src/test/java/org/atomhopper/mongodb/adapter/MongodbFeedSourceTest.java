package org.atomhopper.mongodb.adapter;

import org.apache.abdera.Abdera;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.mongodb.domain.PersistedEntry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class MongodbFeedSourceTest {

    public static class WhenSourcingFeeds {

        MongodbFeedSource mongodbFeedSource;
        MongoTemplate mongoTemplate;
        GetFeedRequest getFeedRequest;
        PersistedEntry persistedEntry;
        Abdera abdera;
        Query query;

        @Before
        public void setUp() throws Exception {
            persistedEntry = null;
            abdera = mock(Abdera.class);
            query = mock(Query.class);
            mongodbFeedSource = new MongodbFeedSource();
            mongoTemplate = mock(MongoTemplate.class);
            mongodbFeedSource.setMongoTemplate(mongoTemplate);
            getFeedRequest = mock(GetFeedRequest.class);
            when(mongoTemplate.findOne(query, PersistedEntry.class)).thenReturn(persistedEntry);
            when(getFeedRequest.getFeedName()).thenReturn("namespace/feed");
            when(getFeedRequest.getPageSize()).thenReturn("25");
            when(getFeedRequest.getPageMarker()).thenReturn("");
            when(getFeedRequest.getAbdera()).thenReturn(abdera);
        }

        @Test
        public void shouldSetMongoTemplate() throws Exception {
            MongodbFeedSource mongodbFeedSource = mock(MongodbFeedSource.class);
            mongodbFeedSource.setMongoTemplate(mongoTemplate);
            verify(mongodbFeedSource).setMongoTemplate(mongoTemplate);
        }

        @Test
        public void shouldGetFeedWithoutMarker() throws Exception {
            assertEquals("Should get a 200 response", org.springframework.http.HttpStatus.OK, mongodbFeedSource.getFeed(getFeedRequest).getResponseStatus());
        }

        @Test
        @Ignore
        public void shouldGetFeedWithMarker() throws Exception {

        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldGetFeedInformation() throws Exception {
            mongodbFeedSource.getFeedInformation();
        }
    }
}
