package net.jps.atom.hopper.adapter.impl;

import java.util.Calendar;
import org.apache.abdera.model.Entry;

/**
 *

 */
public class AtomEntry implements Comparable<AtomEntry> {

    private final Entry entry;
    private Calendar updated;

    public AtomEntry(Entry entry) {
        this.entry = entry;

        updated = Calendar.getInstance();
    }

    public Entry getEntry() {
        return entry;
    }

    public Calendar getUpdated() {
        return updated;
    }

    public void updateTimestamp() {
        updated.setTimeInMillis(System.currentTimeMillis());
    }

    @Override
    public int compareTo(AtomEntry t) {
        return getUpdated().compareTo(t.getUpdated());
    }
}
