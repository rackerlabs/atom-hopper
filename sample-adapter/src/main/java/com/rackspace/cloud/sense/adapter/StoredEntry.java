/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.adapter;

import java.util.Calendar;
import org.apache.abdera.model.Entry;

/**
 *
 * @author zinic
 */
public class StoredEntry implements Comparable<StoredEntry> {

    private final Entry storedEntry;
    private final Calendar datestamp;

    public StoredEntry(Entry storedEntry) {
        datestamp = Calendar.getInstance();
        storedEntry.setUpdated(datestamp.getTime());
        
        this.storedEntry = storedEntry;
    }

    public Calendar getDatestamp() {
        return datestamp;
    }

    public Entry getStoredEntry() {
        return storedEntry;
    }

    @Override
    public int compareTo(StoredEntry t) {
        return datestamp.compareTo(t.getDatestamp());
    }
}
