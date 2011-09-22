package org.atomhopper.adapter.impl;

import org.apache.abdera.model.Entry;

import java.util.Calendar;

/**
 *

 */
public class AtomEntry implements Comparable<AtomEntry> {

    private final Entry entry;
    private final Calendar updated;

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
