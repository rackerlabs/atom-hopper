package org.atomhopper.adapter.jpa;

import java.util.Calendar;
import java.util.Collections;
import java.util.Set;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Entries")
public class PersistedEntry {

    @Id
    @Column(name = "EntryID")
    private String entryId;
    
    @Basic(optional = false)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Feed")
    private PersistedFeed feed;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "CategoryEntryReferences",
    joinColumns = {
        @JoinColumn(name = "entryId", referencedColumnName = "EntryID")},
    inverseJoinColumns = {
        @JoinColumn(name = "category", referencedColumnName = "Term")})
    private Set<PersistedCategory> categories;
    
    @Column(name = "EntryBody")
    private String entryBody;
    
    @Basic(optional = false)
    @Column(name = "CreationDate")
    @Temporal(TemporalType.DATE)
    private Calendar creationDate;
    
    @Basic(optional = false)
    @Column(name = "DateLastUpdated")
    @Temporal(TemporalType.DATE)
    private Calendar dateLastUpdated;

    public PersistedEntry() {
        categories = Collections.EMPTY_SET;
    }

    public PersistedEntry(String entryId) {
        this();

        this.entryId = entryId;
    }

    public Calendar getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Calendar creationDate) {
        this.creationDate = creationDate;
    }

    public Calendar getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(Calendar dateLastUpdated) {
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
