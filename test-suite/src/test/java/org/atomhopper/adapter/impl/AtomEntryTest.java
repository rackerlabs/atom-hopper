package org.atomhopper.adapter.impl;

import org.apache.abdera.model.Entry;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Calendar;

import static java.lang.Thread.sleep;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.atomhopper.TestHelper.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

/**
 * User: sbrayman
 * Date: Sep 23, 2011
 */

@RunWith(Enclosed.class)
public class AtomEntryTest {

    public static class WhenCreatingAtomEntry {

        private AtomEntry atomEntry;
        private Entry entry;

        @Before
        public void setUp() throws Exception {
            entry = mock(Entry.class);
            atomEntry = new AtomEntry(entry);
        }

        @Test
        public void shouldNotCreateNullAtomEntry() throws Exception {
            assertNotNull("Atom entry should not be null.", atomEntry);
        }

        @Test
        public void shouldReturnEntry() throws Exception {
            assertSame("Getting entry should return the entry object it was instantiated with.", entry, atomEntry.getEntry());
        }
    }

    public static class WhenUpdatingAtomEntry {

        private AtomEntry atomEntry;
        private Entry entry;
        private Calendar calendar;

        @Before
        public void setUp() throws Exception {
            entry = mock(Entry.class);
            atomEntry = new AtomEntry(entry);
            calendar = Calendar.getInstance();
            calendar.setTime(atomEntry.getUpdated().getTime());
        }

        @Test
        public void shouldReturnSameDate() throws Exception {
            assertEquals("Times should be equal.", calendar.getTime(), atomEntry.getUpdated().getTime());
        }

        @Test
        public void shouldUpdateTimestamp() throws Exception {
            sleep(500);
            atomEntry.updateTimestamp();
            assertNotEquals("Times should not be equal.", calendar.getTime(), atomEntry.getUpdated().getTime());
        }
    }

    public static class WhenComparingAtomEntries {

        private AtomEntry atomEntry;
        private AtomEntry atomEntry1;
        private Entry entry;
        private Entry entry1;

        @Before
        public void setUp() throws Exception {
            entry = mock(Entry.class);
            entry1 = mock(Entry.class);
            atomEntry = new AtomEntry(entry);
            sleep(500);
            atomEntry1 = new AtomEntry(entry1);
        }

        @Test
        public void shouldShowOffset() throws Exception {
            assertTrue("Offset should be a negative value.", atomEntry.compareTo(atomEntry1) < 0);
            assertEquals("Offset should be 0", atomEntry.compareTo(atomEntry), 0);
        }
    }
}
