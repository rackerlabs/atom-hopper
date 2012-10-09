package org.atomhopper.migration.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import static junit.framework.Assert.assertEquals;
import org.apache.abdera.model.Entry;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.migration.domain.MigrationReadFrom;
import org.atomhopper.migration.domain.MigrationWriteTo;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;

@RunWith(Enclosed.class)
public class MigrationFeedPublisherTest {

    public static class WhenPostingEntries {

        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;
        private MigrationFeedPublisher migrationFeedPublisher;
        private PostEntryRequest postEntryRequest;
        private FeedPublisher oldFeedPublisher;
        private FeedPublisher newFeedPublisher;

        @Before
        public void setUp() throws Exception {
            putEntryRequest = mock(PutEntryRequest.class);
            deleteEntryRequest = mock(DeleteEntryRequest.class);

            oldFeedPublisher = mock(FeedPublisher.class);
            newFeedPublisher = mock(FeedPublisher.class);

            migrationFeedPublisher = new MigrationFeedPublisher();
            migrationFeedPublisher.setNewFeedPublisher(newFeedPublisher);
            migrationFeedPublisher.setOldFeedPublisher(oldFeedPublisher);

            AdapterResponse<Entry> response = ResponseBuilder.created(entry());

            postEntryRequest = mock(PostEntryRequest.class);
            when(postEntryRequest.getEntry()).thenReturn(entry());
            when(postEntryRequest.getFeedName()).thenReturn("namespace/feed");
        }

        @Test
        public void shouldReturnCreatedForWriteToOld() throws Exception {
            migrationFeedPublisher.setWriteTo(MigrationWriteTo.OLD);
            migrationFeedPublisher.setReadFrom(MigrationReadFrom.OLD);

            when(oldFeedPublisher.postEntry(postEntryRequest)).thenReturn(ResponseBuilder.created(entry()));

            AdapterResponse<Entry> adapterResponse = migrationFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test
        public void shouldReturnCreatedForWriteToNew() throws Exception {
            migrationFeedPublisher.setWriteTo(MigrationWriteTo.NEW);
            migrationFeedPublisher.setReadFrom(MigrationReadFrom.NEW);

            when(newFeedPublisher.postEntry(postEntryRequest)).thenReturn(ResponseBuilder.created(entry()));

            AdapterResponse<Entry> adapterResponse = migrationFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test
        public void shouldReturnCreatedForWriteToBoth() throws Exception {
            migrationFeedPublisher.setWriteTo(MigrationWriteTo.BOTH);
            migrationFeedPublisher.setReadFrom(MigrationReadFrom.OLD);

            when(oldFeedPublisher.postEntry(postEntryRequest)).thenReturn(ResponseBuilder.created(entry()));
            when(newFeedPublisher.postEntry(postEntryRequest)).thenReturn(ResponseBuilder.created(entry()));

            AdapterResponse<Entry> adapterResponse = migrationFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldPutEntry() throws Exception {
            migrationFeedPublisher.putEntry(putEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldDeleteEntry() throws Exception {
            migrationFeedPublisher.deleteEntry(deleteEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            migrationFeedPublisher.setParameters(map);
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
