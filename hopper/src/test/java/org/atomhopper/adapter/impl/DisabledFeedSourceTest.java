package org.atomhopper.adapter.impl;


import org.atomhopper.adapter.request.feed.FeedRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;
import org.springframework.http.HttpStatus;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;


@RunWith(Enclosed.class)
public class DisabledFeedSourceTest {

    public static class WhenAccessingDisabledFeedSource {

        private static final String OP_NOT_SUPPORTED_MESSAGE = "Operation not supported";
        private DisabledFeedSource disabledFeedSource;
        private GetEntryRequest getEntryRequest;
        private GetFeedRequest getFeedRequest;

        @Before
        public void setUp() throws Exception {
            disabledFeedSource = DisabledFeedSource.getInstance();
            getEntryRequest = mock(GetEntryRequest.class);
            getFeedRequest = mock(GetFeedRequest.class);
        }

        @Test
        public void shouldGetDisabledFeedSource() {
            assertNotNull("Should not return null", DisabledFeedSource.getInstance());
        }
        
        @Test
        public void shouldGetFeedInformation() {
            assertNotNull("Should not return null", disabledFeedSource.getFeedInformation());
        } 
        
        @Test
        public void shouldGetEntry() {
            AdapterResponse<Entry> response = disabledFeedSource.getEntry(getEntryRequest);
            assertEquals("Should get entry with METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, response.getResponseStatus());
        }   
        
        @Test
        public void shouldGetFeed() {
            AdapterResponse<Feed> response = disabledFeedSource.getFeed(getFeedRequest);
            assertEquals("Should get feed with METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, response.getResponseStatus());
        }        
    }
}
