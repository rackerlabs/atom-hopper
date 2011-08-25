package org.atomhopper.adapter.jpa;

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
}
