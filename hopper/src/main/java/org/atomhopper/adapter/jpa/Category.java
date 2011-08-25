package org.atomhopper.adapter.jpa;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "name")
    private String name;
    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    private Set<FeedEntry> feedEntries;

    public Category() {
        feedEntries = Collections.EMPTY_SET;
    }

    public Category(String name) {
        feedEntries = new HashSet<FeedEntry>();
        
        this.name = name;
    }

    public Set<FeedEntry> getFeedEntries() {
        return feedEntries;
    }

    public void setFeedEntries(Set<FeedEntry> feedEntries) {
        this.feedEntries = feedEntries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Category other = (Category) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
