package org.atomhopper.mongodb.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.mongodb.domain.PersistedEntry;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;

@RunWith(Enclosed.class)
public class MongodbFeedPublisherTest {

    public static class WhenPostingEntries {

        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;
        private MongodbFeedPublisher mongodbFeedPublisher;
        private PostEntryRequest postEntryRequest;
        private MongoTemplate mongoTemplate;

        private PersistedEntry persistedEntry;

        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";
        private final String COLLECTION_NAME = "namespace.feed";


        @Before
        public void setUp() throws Exception {
            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(FEED_NAME);
            persistedEntry.setEntryId(MARKER_ID);
            persistedEntry.setEntryBody(ENTRY_BODY);

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

        @Test
        public void shouldThrowErrorForEntryIdAlreadyExists() throws Exception {
            mongodbFeedPublisher.setAllowOverrideId(true);
            when(mongoTemplate.findOne(any(Query.class), any(Class.class), eq(COLLECTION_NAME))).thenReturn(persistedEntry);
            AdapterResponse<Entry> adapterResponse = mongodbFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 409 (Conflict)",HttpStatus.CONFLICT, adapterResponse.getResponseStatus() );
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
