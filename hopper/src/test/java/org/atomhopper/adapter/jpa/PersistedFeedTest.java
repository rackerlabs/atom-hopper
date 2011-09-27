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
 * Date: Sep 27, 2011
 */

@RunWith(Enclosed.class)
public class PersistedFeedTest {

    public static class WhenCreatingPersistedFeeds {

        private PersistedFeed persistedFeed;
        private PersistedFeed persistedFeedWithInput;

        @Test
        public void shouldCreatePersistedFeed() {
            assertNull(persistedFeed);
            persistedFeed = new PersistedFeed();
            assertNotNull(persistedFeed);
        }

        @Test
        public void shouldCreatePersistedFeedWithParameters() {
            assertNull(persistedFeedWithInput);
            persistedFeedWithInput = new PersistedFeed("name","feedId");
            assertNotNull(persistedFeedWithInput);
            assertNotNull(persistedFeedWithInput.getName());
            assertNotNull(persistedFeedWithInput.getFeedId());
        }
    }

    public static class WhenAccessingPersistedFeeds {

        private PersistedFeed persistedFeed;
        private PersistedEntry persistedEntry;
        private Set<PersistedEntry> persistedEntrySet;

        @Before
        public void setUp() {
            persistedFeed = new PersistedFeed();
            persistedEntry = new PersistedEntry();
            persistedEntrySet = new HashSet();
            persistedEntrySet.add(persistedEntry);
        }

        @Test
        public void shouldSetEntries() {
            assertTrue(persistedFeed.getEntries().isEmpty());
            persistedFeed.setEntries(persistedEntrySet);
            assertFalse(persistedFeed.getEntries().isEmpty());
        }

        @Test
        public void shouldSetFeedId() {
            assertNull(persistedFeed.getFeedId());
            persistedFeed.setFeedId("feedId");
            assertNotNull(persistedFeed.getFeedId());
        }

        @Test
        public void shouldSetName() {
            assertNull(persistedFeed.getName());
            persistedFeed.setName("name");
            assertNotNull(persistedFeed.getName());
        }
    }
}
