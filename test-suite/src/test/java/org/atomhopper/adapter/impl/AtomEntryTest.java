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
        public void shouldNotCreateNullAtomEntry() {
            assertNotNull(atomEntry);
        }

        @Test
        public void shouldReturnEntry() throws Exception {
            assertSame(entry, atomEntry.getEntry());
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
            calendar.setTimeInMillis(atomEntry.getUpdated().getTimeInMillis());
        }

        @Test
        public void shouldReturnSameDate() throws Exception {
            assertEquals(calendar.getTimeInMillis(), atomEntry.getUpdated().getTimeInMillis());
        }

        @Test
        public void shouldUpdateTimestamp() throws Exception {
            atomEntry.updateTimestamp();
            assertTrue(calendar.getTime() != atomEntry.getUpdated().getTime());
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
            assertTrue(atomEntry.compareTo(atomEntry1) < 0);
            assertTrue(atomEntry.compareTo(atomEntry) == 0);
        }
    }
}
