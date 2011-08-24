package net.jps.atom.hopper.adapter.hibernate.impl.domain;

import java.util.Collections;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "entries")
public class FeedEntry {

    @Id
    @Column(name = "entryId")
    private String entryId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "feed")
    private Feed feed;
    
    @ManyToMany(fetch= FetchType.LAZY)
    @JoinTable(name = "CategoryEntryReferences",
        joinColumns = {
            @JoinColumn(name = "entryId", referencedColumnName = "entryId")},
        inverseJoinColumns = {
            @JoinColumn(name = "category", referencedColumnName = "name")})
    private Set<Category> categories;
        
    @Column(name = "entryBody")
    private String entryBody;

    public FeedEntry() {
        categories = Collections.EMPTY_SET;
    }
    
    public FeedEntry(String entryId) {
        this();
        
        this.entryId = entryId;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
}
