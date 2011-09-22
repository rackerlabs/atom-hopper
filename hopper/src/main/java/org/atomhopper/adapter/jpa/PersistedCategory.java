package org.atomhopper.adapter.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Categories")
public class PersistedCategory implements Serializable {

    @Id
    @Column(name = "Term")
    private String term;
    
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<PersistedEntry> feedEntries;

    public PersistedCategory() {
        feedEntries = Collections.EMPTY_SET;
    }

    public PersistedCategory(String term) {
        feedEntries = new HashSet<PersistedEntry>();
        
        this.term = term;
    }

    public Set<PersistedEntry> getFeedEntries() {
        return feedEntries;
    }

    public void setFeedEntries(Set<PersistedEntry> feedEntries) {
        this.feedEntries = feedEntries;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersistedCategory other = (PersistedCategory) obj;
        return !((this.term == null) ? (other.term != null) : !this.term.equals(other.term));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.term != null ? this.term.hashCode() : 0);
        return hash;
    }
}
