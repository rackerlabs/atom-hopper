package org.atomhopper.adapter.jpa;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

import static org.atomhopper.util.TestHelper.assertNotEquals;
import static org.junit.Assert.assertEquals;
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

        private PersistedCategory persistedCategory;

        @Before
        public void setUp() throws Exception {
            persistedCategory = new PersistedCategory();
        }

        @Test
        public void shouldNotBeNull() throws Exception {
            assertNotNull("This should not be a null object.", persistedCategory);
        }

        @Test
        public void shouldContainEmptySet() throws Exception {
            assertTrue(persistedCategory.getFeedEntries().isEmpty());
        }

        @Test
        public void shouldContainTerm() throws Exception {
            assertNull("This should be a null object.", persistedCategory.getTerm());
            persistedCategory = new PersistedCategory("term");
            assertNotNull("This should not be a null object.", persistedCategory.getTerm());
        }
    }

    public static class WhenAccessingPersistedCategories {

        private PersistedCategory persistedCategory;
        private PersistedCategory persistedCategoryNull;
        private PersistedEntry persistedEntry0;
        private PersistedEntry persistedEntry1;
        private Set<PersistedEntry> persistedEntries;
        private Object rightTypeObject;
        private Object nullObject;
        private Object wrongTypeObject;

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
        public void shouldReturnEntries() throws Exception {
            assertFalse("Getting feed entries should not return anything.", persistedCategory.getFeedEntries().size() > 0);
            persistedCategory.setFeedEntries(persistedEntries);
            assertTrue("Now getting feed entries should return at least one.", persistedCategory.getFeedEntries().size() > 0);
        }

        @Test
        public void shouldReturnTerm() throws Exception {
            assertNull("Getting terms should return null.", persistedCategory.getTerm());
            persistedCategory.setTerm("term");
            assertNotNull("Getting terms should not return a null object.", persistedCategory.getTerm());
        }

        @Test
        public void shouldCompareObjects() throws Exception {
            assertEquals("Comparison should return true with same type of object.", persistedCategory, rightTypeObject);
            assertNotEquals("Comparison should return false with different type of object.", persistedCategory, wrongTypeObject);
        }

        @Test
        public void shouldReturnHash() throws Exception {
            assertNotEquals("Hash code should something other than 0.", persistedCategory.hashCode(), 0);
        }
    }
}
