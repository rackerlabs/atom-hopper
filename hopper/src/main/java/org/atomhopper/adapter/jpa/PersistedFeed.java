package org.atomhopper.adapter.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Feeds")
public class PersistedFeed implements Serializable {

    @Id
    @Column(name = "Name")
    private String name;
    
    @Column(name = "FeedID")
    private String feedId;
    
    @OneToMany(mappedBy = "feed", fetch = FetchType.LAZY)
    private Set<PersistedEntry> entries;

    public PersistedFeed() {
        entries = Collections.EMPTY_SET;
    }

    public PersistedFeed(String name, String feedId) {
        entries = new HashSet<PersistedEntry>();
        
        this.feedId = feedId;
        this.name = name;
    }
    
    public Set<PersistedEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<PersistedEntry> entries) {
        this.entries = entries;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
