package org.atomhopper.adapter.jpa;

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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

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
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    @Basic(optional = false)
    @Column(name = "DateLastUpdated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateLastUpdated;

    public PersistedEntry() {
        categories = Collections.EMPTY_SET;
        
        final Calendar localNow = Calendar.getInstance(TimeZone.getDefault());
        localNow.setTimeInMillis(System.currentTimeMillis());
        
        creationDate = localNow.getTime();
        dateLastUpdated = localNow.getTime();
    }

    public PersistedEntry(String entryId) {
        this();

        categories = new HashSet<PersistedCategory>();
        this.entryId = entryId;
    }

    public Date getCreationDate() {
        return (Date) creationDate.clone();
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = (Date) creationDate.clone();
    }

    public Date getDateLastUpdated() {
        return (Date) dateLastUpdated.clone();
    }

    public void setDateLastUpdated(Date dateLastUpdated) {
        this.dateLastUpdated = (Date) dateLastUpdated.clone();
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
