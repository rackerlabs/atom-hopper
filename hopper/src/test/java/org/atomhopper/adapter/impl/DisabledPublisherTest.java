package org.atomhopper.adapter.impl;


import org.atomhopper.adapter.request.feed.FeedRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.apache.abdera.model.Entry;
import org.atomhopper.response.AdapterResponse;
import org.springframework.http.HttpStatus;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.atomhopper.adapter.request.adapter.*;
import org.atomhopper.response.EmptyBody;
import static org.mockito.Mockito.mock;


@RunWith(Enclosed.class)
public class DisabledPublisherTest {

    public static class WhenAccessingDisabledPublisher {

        private DisabledPublisher disabledPublisher;
        private DeleteEntryRequest mockDeleteEntryRequest;
        private PostEntryRequest mockPostEntryRequest;
        private PutEntryRequest mockPutEntryRequest;

        @Before
        public void setUp() throws Exception {
            disabledPublisher = DisabledPublisher.getInstance();
            mockDeleteEntryRequest = mock(DeleteEntryRequest.class);
            mockPostEntryRequest = mock(PostEntryRequest.class);
            mockPutEntryRequest = mock(PutEntryRequest.class);
        }

        @Test
        public void shouldGetDisabledFeedSource() {
            assertNotNull("Should not return null", DisabledFeedSource.getInstance());
        }
        
        @Test
        public void shouldDeleteEntry() {
            AdapterResponse<EmptyBody> response = disabledPublisher.deleteEntry(mockDeleteEntryRequest);
            assertEquals("Should delete entry with METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, response.getResponseStatus());
        }
        
        @Test
        public void shouldPostEntry() {
            AdapterResponse<Entry> response = disabledPublisher.postEntry(mockPostEntryRequest);
            assertEquals("Should post entry with METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, response.getResponseStatus());
        }
        
        @Test
        public void shouldPutEntry() {
            AdapterResponse<Entry> response = disabledPublisher.putEntry(mockPutEntryRequest);
            assertEquals("Should put entry with METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, response.getResponseStatus());
        }        
    }
}
