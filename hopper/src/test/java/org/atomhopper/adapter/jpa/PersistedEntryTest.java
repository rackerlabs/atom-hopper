package org.atomhopper.adapter.jpa;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

/**
 * User: sbrayman
 * Date: Sep 26, 2011
 */

@RunWith(Enclosed.class)
public class PersistedEntryTest {

    public static class WhenCreatingPersistedEntries {

        private PersistedEntry persistedEntryWithEntryId;
        private PersistedEntry persistedEntry;

        @Test
        public void shouldCreatePersistedEntry() throws Exception {
            assertNull("Object should be null.", persistedEntry);
            persistedEntry = new PersistedEntry();
            assertNotNull("Object should no longer be null.", persistedEntry);
        }

        @Test
        public void shouldCreatePersistedEntryWithEntryId() throws Exception {
            persistedEntryWithEntryId = new PersistedEntry("entryId");
            assertNotNull("Getting entry ID should return the object.", persistedEntryWithEntryId.getEntryId());
        }
    }

    public static class WhenAccessingPersistedEntries {

        private PersistedEntry persistedEntry;
        private Instant date;
        private Set<PersistedCategory> persistedCategories;

        @Before
        public void setUp() throws Exception {
            persistedEntry = new PersistedEntry();
        }

        @Test
        public void shouldReturnCreationDate() throws Exception {
            assertNotNull("Getting creation date should not return null.", persistedEntry.getCreationDate());
            date = persistedEntry.getCreationDate();
            assertEquals("Getting creation date should return a Date object.", persistedEntry.getCreationDate(), date);
            persistedEntry.setCreationDate(Instant.now());
            assertNotSame("Setting the creation date should update the object.", persistedEntry.getCreationDate(), date);
        }

        @Test
        public void shouldReturnDateLastUpdated() throws Exception {
            assertNotNull("Getting the date last updated should not return null.", persistedEntry.getDateLastUpdated());
            date = persistedEntry.getDateLastUpdated();
            assertEquals("Getting the date last updated should return a date object.", persistedEntry.getDateLastUpdated(), date);
            persistedEntry.setDateLastUpdated(Instant.now());
            assertNotSame("Setting the date last updated should change last updated date.", persistedEntry.getDateLastUpdated(), date);
        }

        @Test
        public void shouldReturnCategories() throws Exception {
            assertNotNull("Getting categories should not return null.", persistedEntry.getCategories());
            persistedCategories = persistedEntry.getCategories();
            persistedEntry.setCategories(new HashSet<PersistedCategory>());
            assertNotSame("Setting categories should update categories set with new object set.", persistedEntry.getCategories(), persistedCategories);
        }

        @Test
        public void shouldReturnEntryBody() throws Exception {
            assertNull("Getting entry body should return null.", persistedEntry.getEntryBody());
            persistedEntry.setEntryBody("entryBody");
            assertNotNull("Getting entry body should no longer return null.", persistedEntry.getEntryBody());
        }

        @Test
        public void shouldReturnFeed() throws Exception {
            assertNull("Getting feed should return null.", persistedEntry.getFeed());
            persistedEntry.setFeed(new PersistedFeed());
            assertNotNull("Getting feed should no longer return null.", persistedEntry.getFeed());
        }

        @Test
        public void shouldReturnEntryId() throws Exception {
            assertNull("Getting entry ID should return null.", persistedEntry.getEntryId());
            persistedEntry.setEntryId("entryId");
            assertNotNull("Getting entry ID should no longer return null.", persistedEntry.getEntryId());
        }
    }
}
