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
        public void shouldCreatePersistedFeed() throws Exception {
            assertNull("Should be null.", persistedFeed);
            persistedFeed = new PersistedFeed();
            assertNotNull("Should not be null.", persistedFeed);
        }

        @Test
        public void shouldCreatePersistedFeedWithParameters() throws Exception {
            assertNull("Should be null.", persistedFeedWithInput);
            persistedFeedWithInput = new PersistedFeed("name","feedId");
            assertNotNull("Should not be null.", persistedFeedWithInput);
            assertNotNull("Getting the name should not return null.", persistedFeedWithInput.getName());
            assertNotNull("Getting the feed ID should not return null.", persistedFeedWithInput.getFeedId());
        }
    }

    public static class WhenAccessingPersistedFeeds {

        private PersistedFeed persistedFeed;
        private PersistedEntry persistedEntry;
        private Set<PersistedEntry> persistedEntrySet;

        @Before
        public void setUp() throws Exception {
            persistedFeed = new PersistedFeed();
            persistedEntry = new PersistedEntry();
            persistedEntrySet = new HashSet();
            persistedEntrySet.add(persistedEntry);
        }

        @Test
        public void shouldSetEntries() throws Exception {
            assertTrue("Getting entries should return empty.", persistedFeed.getEntries().isEmpty());
            persistedFeed.setEntries(persistedEntrySet);
            assertFalse("Now getting entries should not return empty.", persistedFeed.getEntries().isEmpty());
        }

        @Test
        public void shouldSetFeedId() throws Exception {
            assertNull("Getting feed ID should return null.", persistedFeed.getFeedId());
            persistedFeed.setFeedId("feedId");
            assertNotNull("Now getting the feed ID should not return null.", persistedFeed.getFeedId());
        }

        @Test
        public void shouldSetName() throws Exception {
            assertNull(persistedFeed.getName());
            persistedFeed.setName("name");
            assertNotNull(persistedFeed.getName());
        }
    }
}
