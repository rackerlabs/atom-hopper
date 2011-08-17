package net.jps.atom.hopper.adapter.hibernate.impl.domain;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private int id;
    
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
