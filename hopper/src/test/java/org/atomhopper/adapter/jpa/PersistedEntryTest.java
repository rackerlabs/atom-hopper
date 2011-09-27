package org.atomhopper.adapter.jpa;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        public void shouldCreatePersistedEntry() {
            assertNull(persistedEntry);
            persistedEntry = new PersistedEntry();
            assertNotNull(persistedEntry);
        }

        @Test
        public void shouldCreatePersistedEntryWithEntryId() {
            assertNull(persistedEntryWithEntryId);
            persistedEntryWithEntryId = new PersistedEntry("entryId");
            assertTrue(persistedEntryWithEntryId.getEntryId().length() > 0);
        }
    }

    public static class WhenAccessingPersistedEntries {

        private PersistedEntry persistedEntry;
        private Date date;

        @Before
        public void setUp() {
            persistedEntry = new PersistedEntry();
        }

        @Test
        public void shouldReturnCreationDate() {
            assertNotNull(persistedEntry.getCreationDate());
            date = persistedEntry.getCreationDate();
            assertTrue(persistedEntry.getCreationDate() == date);
            persistedEntry.setCreationDate(new Date());
            assertFalse(persistedEntry.getCreationDate() == date);
        }

        @Test
        public void shouldReturnDateLastUpdated() {
            assertNotNull(persistedEntry.getDateLastUpdated());
            date = persistedEntry.getDateLastUpdated();
            assertTrue(persistedEntry.getDateLastUpdated() == date);
            persistedEntry.setDateLastUpdated(new Date());
            assertFalse(persistedEntry.getDateLastUpdated() == date);
        }

        @Test
        public void shouldReturnCategories() {
            assertNotNull(persistedEntry.getCategories());
            persistedEntry.setCategories(new HashSet<PersistedCategory>());
            assertNotNull(persistedEntry.getCategories());
        }

        @Test
        public void shouldReturnEntryBody() {
            assertNull(persistedEntry.getEntryBody());
            persistedEntry.setEntryBody("entryBody");
            assertNotNull(persistedEntry.getEntryBody());
        }

        @Test
        public void shouldReturnFeed() {
            assertNull(persistedEntry.getFeed());
            persistedEntry.setFeed(new PersistedFeed());
            assertNotNull(persistedEntry.getFeed());
        }

        @Test
        public void shouldReturnEntryId() {
            assertNull(persistedEntry.getEntryId());
            persistedEntry.setEntryId("entryId");
            assertNotNull(persistedEntry.getEntryId());
        }
    }
}
