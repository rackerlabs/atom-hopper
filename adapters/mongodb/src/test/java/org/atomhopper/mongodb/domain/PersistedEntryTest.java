package org.atomhopper.mongodb.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;
import static junit.framework.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class PersistedEntryTest {

    public static class WhenUsingPersistedEntries {

        private PersistedEntry persistedEntry;
        private PersistedCategory persistedCategory;
        private final String ID = UUID.randomUUID().toString();
        private final String ENTRY_BODY = "body";
        private final String FEED = "namespace/feed";
        private final String PERSISTED_CATEGORY_VALUE1 = "MyCategory1";
        private final String PERSISTED_CATEGORY_VALUE2 = "MyCategory2";

        @Before
        public void setUp() throws Exception {
            persistedEntry = new PersistedEntry();
            persistedCategory = new PersistedCategory(PERSISTED_CATEGORY_VALUE1);

            persistedEntry.setEntryId(ID);
            persistedEntry.setEntryBody(ENTRY_BODY);
            persistedEntry.setFeed(FEED);
            persistedEntry.addCategory(persistedCategory);
        }

        @Test
        public void shouldReturnCreationDate() throws Exception {
            assertNotNull(persistedEntry.getCreationDate());
        }

        @Test
        public void shouldReturnDateLastUpdated() throws Exception {
            assertNotNull(persistedEntry.getDateLastUpdated());
        }

        @Test
        public void shouldSetDateFields() throws Exception {
            final Calendar localNow = Calendar.getInstance(TimeZone.getDefault());
            localNow.setTimeInMillis(System.currentTimeMillis());
            Date dateToSet = localNow.getTime();

            persistedEntry.setCreationDate(dateToSet);
            assertEquals("The creation date should be able to be set and read back", dateToSet, persistedEntry.getCreationDate());

            persistedEntry.setDateLastUpdated(dateToSet);
            assertEquals("The date last updated should be able to be set and read back", dateToSet, persistedEntry.getDateLastUpdated());
        }

        @Test
        public void shouldReturnId() throws Exception {
            assertEquals("IDs should match", ID, persistedEntry.getEntryId());
        }

        @Test
        public void shouldReturnEntryBody() throws Exception {
            assertEquals("Entry body should match", ENTRY_BODY, persistedEntry.getEntryBody());
        }

        @Test
        public void shouldReturnFeed() throws Exception {
            assertEquals("Feed should match", FEED, persistedEntry.getFeed());
        }

        @Test
        public void shouldContainCategories() throws Exception {
            assertNotNull(persistedEntry.getCategories());
        }

        @Test
        public void shouldGetAndSetCategory() throws Exception {
            assertEquals("Category should match", PERSISTED_CATEGORY_VALUE1, persistedCategory.getValue());
            persistedCategory.setValue(PERSISTED_CATEGORY_VALUE2);
            assertEquals("Category should match", PERSISTED_CATEGORY_VALUE2, persistedCategory.getValue());
        }

        @Test
        public void shouldDisplayContentsViaToString() throws Exception {
            assertNotNull(persistedEntry.toString());
        }
    }
}
