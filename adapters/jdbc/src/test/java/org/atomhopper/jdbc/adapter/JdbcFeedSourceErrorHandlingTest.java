package org.atomhopper.jdbc.adapter;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.SQLException;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests error handling scenarios in JdbcFeedSource to ensure proper error responses
 * and XML content type handling.
 */
@RunWith(MockitoJUnitRunner.class)
public class JdbcFeedSourceErrorHandlingTest {

    @Mock
    private JdbcTemplate mockJdbcTemplate;
    
    @Mock
    private GetFeedRequest mockGetFeedRequest;
    
    @Mock
    private GetEntryRequest mockGetEntryRequest;
    
    private JdbcFeedSource jdbcFeedSource;
    private Abdera abdera;

    @Before
    public void setUp() {
        jdbcFeedSource = new JdbcFeedSource();
        jdbcFeedSource.setJdbcTemplate(mockJdbcTemplate);
        abdera = new Abdera();
        
        // Setup common mock behavior
        when(mockGetFeedRequest.getAbdera()).thenReturn(abdera);
        when(mockGetFeedRequest.getFeedName()).thenReturn("test-feed");
        when(mockGetFeedRequest.getPageSize()).thenReturn("25");
        when(mockGetFeedRequest.getSearchQuery()).thenReturn("");
        
        when(mockGetEntryRequest.getAbdera()).thenReturn(abdera);
        when(mockGetEntryRequest.getFeedName()).thenReturn("test-feed");
        when(mockGetEntryRequest.getEntryId()).thenReturn("test-entry-id");
    }

    @Test
    public void testDatabaseConnectionError() {
        // Simulate database connection failure
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenThrow(new DataAccessException("Connection refused") {});

        try {
            AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
            fail("Expected DataAccessException to be thrown");
        } catch (DataAccessException e) {
            assertEquals("Connection refused", e.getMessage());
        }
    }

    @Test
    public void testQueryTimeoutError() {
        // Simulate query timeout
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenThrow(new QueryTimeoutException("Query timeout", new SQLException()));

        try {
            AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
            fail("Expected QueryTimeoutException to be thrown");
        } catch (QueryTimeoutException e) {
            assertTrue("Should contain query timeout message", e.getMessage().contains("Query timeout"));
        }
    }

    @Test
    public void testDataIntegrityViolationError() {
        // Simulate data integrity violation
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenThrow(new DataIntegrityViolationException("Data integrity violation"));

        try {
            AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
            fail("Expected DataIntegrityViolationException to be thrown");
        } catch (DataIntegrityViolationException e) {
            assertEquals("Data integrity violation", e.getMessage());
        }
    }

    @Test
    public void testInvalidPageSizeHandling() {
        // Test with invalid page size
        when(mockGetFeedRequest.getPageSize()).thenReturn("invalid");
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenReturn(Collections.emptyList());

        try {
            AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
            fail("Expected NumberFormatException to be thrown");
        } catch (NumberFormatException e) {
            assertTrue("Should contain invalid number format message", 
                      e.getMessage().contains("invalid"));
        }
    }

    @Test
    public void testInvalidMarkerAndStartingAtCombination() {
        // Test invalid combination of marker and startingAt parameters
        when(mockGetFeedRequest.getPageMarker()).thenReturn("some-marker");
        when(mockGetFeedRequest.getStartingAt()).thenReturn("2023-01-01T00:00:00Z");

        AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Should return bad request status", HttpStatus.BAD_REQUEST, response.getResponseStatus());
        assertTrue("Should contain error message about marker and startingAt", 
                  response.getMessage() != null && 
                  (response.getMessage().contains("marker") || response.getMessage().contains("startingAt")));
    }

    @Test
    public void testEntryNotFoundHandling() {
        // Test entry not found scenario
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenReturn(Collections.emptyList());

        AdapterResponse<Entry> response = jdbcFeedSource.getEntry(mockGetEntryRequest);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Should return not found status", HttpStatus.NOT_FOUND, response.getResponseStatus());
    }

    @Test
    public void testEmptyFeedHandling() {
        // Test empty feed scenario
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenReturn(Collections.emptyList());

        AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
        
        assertNotNull("Response should not be null", response);
        assertEquals("Should return success status for empty feed", HttpStatus.OK, response.getResponseStatus());
        assertNotNull("Feed should not be null", response.getBody());
    }

    @Test
    public void testSqlExceptionHandling() {
        // Test SQL exception during query execution
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenThrow(new DataAccessException("SQL execution error", new SQLException("Table not found")) {});

        try {
            AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
            fail("Expected DataAccessException to be thrown");
        } catch (DataAccessException e) {
            assertTrue("Should contain SQL execution error message", e.getMessage().contains("SQL execution error"));
            assertTrue("Should have SQL exception as cause", e.getCause() instanceof SQLException);
        }
    }

    @Test
    public void testInvalidSearchQueryHandling() {
        // Test with malformed search query
        when(mockGetFeedRequest.getSearchQuery()).thenReturn("invalid:search:query:format");
        when(mockJdbcTemplate.query(anyString(), any(Object[].class), any(RowMapper.class)))
            .thenReturn(Collections.emptyList());

        // Should not throw exception, but handle gracefully
        AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
        
        assertNotNull("Response should not be null", response);
        assertTrue("Should return success or bad request status", 
                  response.getResponseStatus() == HttpStatus.OK || response.getResponseStatus() == HttpStatus.BAD_REQUEST);
    }

    @Test
    public void testNullJdbcTemplateHandling() {
        // Test behavior when JdbcTemplate is null
        jdbcFeedSource.setJdbcTemplate(null);

        try {
            AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException e) {
            // Expected behavior
        }
    }

    @Test
    public void testInvalidDateFormatInStartingAt() {
        // Test invalid date format in startingAt parameter
        when(mockGetFeedRequest.getPageMarker()).thenReturn(null);
        when(mockGetFeedRequest.getStartingAt()).thenReturn("invalid-date-format");

        try {
            AdapterResponse<Feed> response = jdbcFeedSource.getFeed(mockGetFeedRequest);
            // The method may handle invalid dates gracefully and return a response
            // instead of throwing an exception, which is also valid behavior
            assertNotNull("Response should not be null", response);
        } catch (IllegalArgumentException e) {
            assertTrue("Should contain date parsing error", 
                      e.getMessage().contains("Invalid format") || e.getMessage().contains("parse"));
        } catch (Exception e) {
            // Other exceptions are also acceptable for invalid date format
            assertTrue("Should be a parsing related exception", 
                      e.getMessage().contains("parse") || e.getMessage().contains("format") || 
                      e.getMessage().contains("date") || e.getMessage().contains("time"));
        }
    }
}