package org.atomhopper.mongodb.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;

@RunWith(Enclosed.class)
public class MongodbFeedPublisherTest {

    public static class WhenPostingEntries {

        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;
        private MongodbFeedPublisher mongodbFeedPublisher;
        private PostEntryRequest postEntryRequest;
        private MongoTemplate mongoTemplate;


        @Before
        public void setUp() throws Exception {
            putEntryRequest = mock(PutEntryRequest.class);
            deleteEntryRequest = mock(DeleteEntryRequest.class);
            mongoTemplate = mock(MongoTemplate.class);
            mongodbFeedPublisher = new MongodbFeedPublisher();
            mongodbFeedPublisher.setMongoTemplate(mongoTemplate);
            postEntryRequest = mock(PostEntryRequest.class);
            when(postEntryRequest.getEntry()).thenReturn(entry());
            when(postEntryRequest.getFeedName()).thenReturn("namespace/feed");
        }

        @Test
        public void shouldReturnHTTPCreated() throws Exception {
            AdapterResponse<Entry> adapterResponse = mongodbFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldPutEntry() throws Exception {
            mongodbFeedPublisher.putEntry(putEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldDeleteEntry() throws Exception {
            mongodbFeedPublisher.deleteEntry(deleteEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            mongodbFeedPublisher.setParameters(map);
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
