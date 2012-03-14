package org.atomhopper.adapter;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;


@RunWith(Enclosed.class)
public class ResponseBuilderTest {

    public static class WhenAccessingResponseBuilder {

        private static final String MESSAGE = "message";
        private Feed mockFeed;
        private Entry mockEntry;

        @Before
        public void setUp() throws Exception {
            mockFeed = mock(Feed.class);
            mockEntry = mock(Entry.class);
        }
        
        @Test
        public void shouldReturnNotImplementedResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.notImplemented(MESSAGE);
            assertEquals("Should return correct message", MESSAGE, response.getMessage());
            assertEquals("Should return correct HttpStatus METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, response.getResponseStatus());
        }        

        @Test
        public void shouldReturnBadRequestResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.badRequest(MESSAGE);
            assertEquals("Should return correct message", MESSAGE, response.getMessage());
            assertEquals("Should return HttpStatus BAD_REQUEST", HttpStatus.BAD_REQUEST, response.getResponseStatus());
        }
        
        @Test
        public void shouldReturnNotFoundResponse() throws Exception {
            AdapterResponse<Feed> response = ResponseBuilder.notFound(MESSAGE);
            assertEquals("Should return HttpStatus NOT_FOUND", HttpStatus.NOT_FOUND, response.getResponseStatus());
        }        
       
        @Test
        public void shouldReturnFeedFoundResponse() throws Exception {
            AdapterResponse<Feed> response = ResponseBuilder.found(mockFeed);
            assertEquals("Should return HttpStatus OK", HttpStatus.OK, response.getResponseStatus());
        }
        
        @Test
        public void shouldReturnEntryFoundResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.found(mockEntry);
            assertEquals("Should return HttpStatus OK", HttpStatus.OK, response.getResponseStatus());
        } 
        
        @Test
        public void shouldReturnReplyResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.reply(HttpStatus.FORBIDDEN, MESSAGE);
            assertEquals("Should return correct message", MESSAGE, response.getMessage());
            assertEquals("Should return HttpStatus FORBIDDEN", HttpStatus.FORBIDDEN, response.getResponseStatus());
        } 
        
        @Test
        public void shouldReturnErrorResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.error(MESSAGE);
            assertEquals("Should return correct message", MESSAGE, response.getMessage());
            assertEquals("Should return HttpStatus INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, response.getResponseStatus());
        }        
        
        @Test
        public void shouldReturnEntryUpdatedResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.updated(mockEntry, MESSAGE);
            assertEquals("Should return correct message", MESSAGE, response.getMessage());
            assertEquals("Should return HttpStatus ACCEPTED", HttpStatus.ACCEPTED, response.getResponseStatus());
        }
        
        @Test
        public void shouldReturnEntryCreatedResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.created(mockEntry);
            assertEquals("Should return HttpStatus CREATED", HttpStatus.CREATED, response.getResponseStatus());
        } 
        
        @Test
        public void shouldReturnEntryCreatedWithMessageResponse() throws Exception {
            AdapterResponse<Entry> response = ResponseBuilder.created(mockEntry, MESSAGE);
            assertEquals("Should return correct message", MESSAGE, response.getMessage());
            assertEquals("Should return HttpStatus CREATED", HttpStatus.CREATED, response.getResponseStatus());
        }        
    }
}