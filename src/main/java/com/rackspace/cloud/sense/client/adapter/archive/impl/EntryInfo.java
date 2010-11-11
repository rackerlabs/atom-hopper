/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rackspace.cloud.sense.client.adapter.archive.impl;

import java.util.Date;
import org.apache.abdera.model.Entry;

/**
 *
 * @author zinic
 */
public class EntryInfo {

    private final String id;
    private final Entry entry;

    public EntryInfo(String id) {
        this(id, null);
    }

    public EntryInfo(String id, Entry e) {
        this.id = id;
        this.entry = e;
    }

    public Entry getEntry() {
        return entry;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final EntryInfo other = (EntryInfo) obj;

        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.id != null ? this.id.hashCode() : 0);

        return hash;
    }
}
