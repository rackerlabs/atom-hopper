package org.atomhopper.adapter.jpa;

import java.io.Serializable;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.atomhopper.util.NanoClock;

@Entity
@Table(name = "Entries")
public class PersistedEntry implements Serializable {

    @Id
    @Column(name = "EntryID")
    private String entryId;
    
    @Basic(optional = false)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Feed")
    private PersistedFeed feed;
    
    @ManyToMany(fetch = FetchType.LAZY, cascade={CascadeType.ALL})
    @JoinTable(name = "CategoryEntryReferences",
    joinColumns = {
        @JoinColumn(name = "entryId", referencedColumnName = "EntryID")},
    inverseJoinColumns = {
        @JoinColumn(name = "category", referencedColumnName = "Term")})
    private Set<PersistedCategory> categories;
    
    @Column(name = "EntryBody")
    @Lob
    private String entryBody;
    
    @Basic(optional = false)
    @Column(name = "CreationDate")
    private Instant creationDate;
    
    @Basic(optional = false)
    @Column(name = "DateLastUpdated")
    private Instant dateLastUpdated;

    public PersistedEntry() {
        categories = Collections.EMPTY_SET;
        Clock nanoClock = new NanoClock();
        Instant now = Instant.now(nanoClock);
        creationDate = now;
        dateLastUpdated = now;
    }

    public PersistedEntry(String entryId) {
        this();

        categories = new HashSet<PersistedCategory>();
        this.entryId = entryId;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(Instant dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    public Set<PersistedCategory> getCategories() {
        return categories;
    }

    public void setCategories(Set<PersistedCategory> categories) {
        this.categories = categories;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }

    public PersistedFeed getFeed() {
        return feed;
    }

    public void setFeed(PersistedFeed feed) {
        this.feed = feed;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
}
