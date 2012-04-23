package org.atomhopper.mongodb.adapter;

import org.apache.abdera.model.Entry;
import org.apache.abdera.protocol.server.RequestContext;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.adapter.request.adapter.impl.PostEntryRequestImpl;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(Enclosed.class)
public class MongodbFeedPublisherTest {

    public static class WhenPostingEntries {

        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;
        private MongodbFeedPublisher mongodbFeedPublisher;
        private PostEntryRequest postEntryRequest;
        private RequestContext requestContext;


        @Before
        public void setUp() throws Exception {
            putEntryRequest = mock(PutEntryRequest.class);
            deleteEntryRequest = mock(DeleteEntryRequest.class);
            mongodbFeedPublisher = new MongodbFeedPublisher();
            requestContext = mock(RequestContext.class);
            postEntryRequest = new PostEntryRequestImpl(requestContext);
        }

        @Test
        @Ignore //TODO: Feed this test a correctly mocked entry.
        public void shouldReturnFeedSourceAdapterResponseWithCategory() throws Exception {
            Entry entry = postEntryRequest.getEntry();
            entry.addCategory("category");
            AdapterResponse adapterResponse = mongodbFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Category added to the PostEntryRequest should come back in the AdapterResponse", "category", adapterResponse);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldPutEntry() throws Exception {
            mongodbFeedPublisher.putEntry(putEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldDeleteEntry() throws Exception {
            mongodbFeedPublisher.deleteEntry(deleteEntryRequest);
        }
    }
}
