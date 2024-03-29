package org.atomhopper.jdbc.adapter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Link;
import org.apache.abdera.parser.stax.FOMEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.jdbc.model.PersistedEntry;
import org.atomhopper.jdbc.query.PostgreSQLTextArray;
import org.atomhopper.response.AdapterResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(Enclosed.class)
public class JdbcFeedPublisherTest {

    public static class WhenPostingEntries {

        private PutEntryRequest putEntryRequest;
        private DeleteEntryRequest deleteEntryRequest;
        private JdbcFeedPublisher jdbcFeedPublisher;
        private PostEntryRequest postEntryRequest;
        private JdbcTemplate jdbcTemplate;

        private final String MARKER_ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "<entry xmlns='http://www.w3.org/2005/Atom'></entry>";
        private final String FEED_NAME = "namespace/feed";
        private PersistedEntry persistedEntry;
        private List<PersistedEntry> entryList;

        @Before
        public void setUp() throws Exception {
            putEntryRequest = mock(PutEntryRequest.class);
            deleteEntryRequest = mock(DeleteEntryRequest.class);
            jdbcTemplate = mock(JdbcTemplate.class);
            jdbcFeedPublisher = new JdbcFeedPublisher();
            jdbcFeedPublisher.setJdbcTemplate(jdbcTemplate);
            postEntryRequest = mock(PostEntryRequest.class);
            when(postEntryRequest.getEntry()).thenReturn(entry());
            when(postEntryRequest.getFeedName()).thenReturn("namespace/feed");

            persistedEntry = new PersistedEntry();
            persistedEntry.setFeed(FEED_NAME);
            persistedEntry.setEntryId(MARKER_ID);
            persistedEntry.setEntryBody(ENTRY_BODY);

            entryList = new ArrayList<PersistedEntry>();
            entryList.add(persistedEntry);
        }


        @Test( expected = IllegalArgumentException.class )
        public void shouldThrowExceptionForPrefixColumnMap() throws Exception {

            jdbcFeedPublisher.setDelimiter( ":" );
            jdbcFeedPublisher.afterPropertiesSet();
        }

        @Test( expected = IllegalArgumentException.class )
        public void shouldThrowExceptionForDelimiter() throws Exception {

            Map<String, String> map = new HashMap<String, String>();
            map.put( "test1", "testA" );

            jdbcFeedPublisher.setPrefixColumnMap( map );
            jdbcFeedPublisher.afterPropertiesSet();
        }

        @Test
        public void shouldReturnHTTPCreated() throws Exception {
            AdapterResponse<Entry> adapterResponse = jdbcFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test
        public void shouldThrowErrorForEntryIdAlreadyExists() throws Exception {
            jdbcFeedPublisher.setAllowOverrideId(true);
            // there are now 2 flavors of jdbcTemplate.update()
            when(jdbcTemplate.update(anyString(), new Object[]{
                    anyString(), anyString(), anyString(), any(PostgreSQLTextArray.class)
            })).thenThrow(new DuplicateKeyException("duplicate entry"));
            when(jdbcTemplate.update(anyString(), new Object[]{
                    anyString(), any(java.util.Date.class), any(java.util.Date.class),
                    anyString(), anyString(), any(PostgreSQLTextArray.class)
            })).thenThrow(new DuplicateKeyException("duplicate entry"));
            Map<String, String> prefixMap = new HashMap<String, String>();
            prefixMap.put("foo", "bar");
            prefixMap.put("gee", "wish");
            jdbcFeedPublisher.setPrefixColumnMap(prefixMap);
            AdapterResponse<Entry> adapterResponse = jdbcFeedPublisher.postEntry(postEntryRequest);
            assertEquals("Should return HTTP 409 (Conflict)", HttpStatus.CONFLICT, adapterResponse.getResponseStatus());
        }
        
        @Test
        public void postEntryWithAllowOverrideDate() {
        	jdbcFeedPublisher.setEnableTimers(true);
        	jdbcFeedPublisher.setAllowOverrideDate(true);
        	Entry abderaParsedEntry = Mockito.mock(Entry.class);
        	Mockito.when(postEntryRequest.getEntry()).thenReturn(abderaParsedEntry);
        	Mockito.when(abderaParsedEntry.getUpdated()).thenReturn(new Date());
        	Link link = Mockito.mock(Link.class);
			Mockito.when(abderaParsedEntry.getSelfLink()).thenReturn(link );
        	AdapterResponse<Entry> adapterResponse = jdbcFeedPublisher.postEntry(postEntryRequest);
        	
        	assertEquals("Should return HTTP 201 (Created)", HttpStatus.CREATED, adapterResponse.getResponseStatus());
        }

        @Test
        public void shouldNotLowerCaseCategoryInPrefixMap() throws Exception {
            Map<String, String> prefixMap = new HashMap<String, String>();
            prefixMap.put("foo", "bar");
            prefixMap.put("gee", "wish");
            jdbcFeedPublisher.setPrefixColumnMap(prefixMap);

            List<org.apache.abdera.model.Category> categories = new ArrayList<org.apache.abdera.model.Category>();
            org.apache.abdera.model.Category cat1 = mock(org.apache.abdera.parser.stax.FOMCategory.class);
            org.apache.abdera.model.Category cat2 = mock(org.apache.abdera.parser.stax.FOMCategory.class);
            when(cat1.getTerm()).thenReturn("foo:myCamelCaseFoo");
            when(cat2.getTerm()).thenReturn("gee:myCamelCaseGee");
            categories.add(cat1);
            categories.add(cat2);
            String[] results = jdbcFeedPublisher.processCategories(categories);

            assertNotNull("processCategories() should return non null results", results);
            assertEquals("processCategories() should return array of length 2", results.length, 2);

            List<String> resultsArray = Arrays.asList(results);
            for (org.apache.abdera.model.Category cat: categories) {
                assertTrue("category '" + cat.getTerm() + "' should be in the original categories",
                        resultsArray.contains(cat.getTerm()));
            }
        }

        @Test
        public void shouldLowerCaseCategory() throws Exception {
            Map<String, String> prefixMap = new HashMap<String, String>();
            prefixMap.put("foo", "bar");
            prefixMap.put("gee", "wish");
            jdbcFeedPublisher.setPrefixColumnMap(prefixMap);

            String camelCase = "myCamelCaseFood";
            String toBeLowerCase = "toBeLowerCase";
            List<org.apache.abdera.model.Category> categories = new ArrayList<org.apache.abdera.model.Category>();
            org.apache.abdera.model.Category cat1 = mock(org.apache.abdera.parser.stax.FOMCategory.class);
            org.apache.abdera.model.Category cat2 = mock(org.apache.abdera.parser.stax.FOMCategory.class);
            when(cat1.getTerm()).thenReturn("foo:" + camelCase);
            when(cat2.getTerm()).thenReturn("cat2:" + toBeLowerCase);
            categories.add(cat1);
            categories.add(cat2);
            String[] results = jdbcFeedPublisher.processCategories(categories);

            assertNotNull("processCategories() should return non null results", results);
            assertEquals("processCategories() should return array of length 2", results.length, 2);
            for (int idx=0; idx<results.length; idx++) {
                 if ( results[idx].startsWith("cat2:")) {
                    assertEquals("result '" + results[idx] + "' should be in lower case",
                            results[idx], "cat2:" + toBeLowerCase.toLowerCase());
                }
            }
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldPutEntry() throws Exception {
            jdbcFeedPublisher.putEntry(putEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldDeleteEntry() throws Exception {
            jdbcFeedPublisher.deleteEntry(deleteEntryRequest);
        }

        @Test(expected = UnsupportedOperationException.class)
        public void shouldSetParameters() throws Exception {
            Map<String, String> map = new HashMap<String, String>();
            map.put("test1", "test2");
            jdbcFeedPublisher.setParameters(map);
        }
        
        
        @Test
        public void testCategories() throws Exception {
            String[] abc = {"test1,test1Result", "result"};
            Map<String, String> map = new HashMap<String, String>();
            map.put( "test1", "result" );
            jdbcFeedPublisher.setPrefixColumnMap(map);
            jdbcFeedPublisher.setDelimiter(",");
			JdbcFeedPublisher.Categories categories = jdbcFeedPublisher.new Categories(abc );
            assertEquals("result", categories.getCategories()[0]);
            assertEquals("test1Result", categories.getPrefix("test1"));
            
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
