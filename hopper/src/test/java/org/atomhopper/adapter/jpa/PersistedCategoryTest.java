package org.atomhopper.adapter.jpa;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * User: sbrayman
 * Date: Sep 26, 2011
 */

@RunWith(Enclosed.class)
public class PersistedCategoryTest {

    public static class WhenCreatingPersistedCategories {

        PersistedCategory persistedCategory;

        @Before
        public void setUp() {
            persistedCategory = new PersistedCategory();
        }

        @Test
        public void shouldNotBeNull() {
            assertNotNull(persistedCategory);
        }

        @Test
        public void shouldContainEmptySet() {
            assertTrue(persistedCategory.getFeedEntries().isEmpty());
        }

        @Test
        public void shouldContainTerm() {
            assertNull(persistedCategory.getTerm());
            persistedCategory = new PersistedCategory("term");
            assertNotNull(persistedCategory.getTerm());
        }
    }

    public static class WhenAccessingPersistedCategories {

        PersistedCategory persistedCategory;
        PersistedCategory persistedCategoryNull;
        PersistedEntry persistedEntry0;
        PersistedEntry persistedEntry1;
        Set<PersistedEntry> persistedEntries;
        Object rightTypeObject;
        Object nullObject;
        Object wrongTypeObject;

        @Before
        public void setUp() throws Exception {
            persistedCategory = new PersistedCategory();
            persistedEntry0 = new PersistedEntry();
            persistedEntry1 = new PersistedEntry();
            persistedEntries = new HashSet();
            persistedEntries.add(persistedEntry0);
            persistedEntries.add(persistedEntry1);
            rightTypeObject = new PersistedCategory();
            nullObject = null;
            wrongTypeObject = new Boolean(true);
        }

        @Test
        public void shouldReturnEntries() {
            assertFalse(persistedCategory.getFeedEntries().size() > 0);
            persistedCategory.setFeedEntries(persistedEntries);
            assertTrue(persistedCategory.getFeedEntries().size() > 0);
        }

        @Test
        public void shouldReturnTerm() {
            assertNull(persistedCategory.getTerm());
            persistedCategory.setTerm("term");
            assertNotNull(persistedCategory.getTerm());
        }

        @Test
        public void shouldCompareObjects() {
            assertFalse(persistedCategory.equals(nullObject));
            assertTrue(persistedCategory.equals(rightTypeObject));
            assertFalse(persistedCategory.equals(wrongTypeObject));
        }

        @Test
        public void shouldReturnHash() {
            assertFalse(persistedCategory.hashCode() == 0);
        }
    }
}
