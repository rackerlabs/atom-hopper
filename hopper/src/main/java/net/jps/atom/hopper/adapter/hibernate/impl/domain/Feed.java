package net.jps.atom.hopper.adapter.hibernate.impl.domain;

import java.util.Collections;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "feeds")
public class Feed {

    @Id
    @Column(name = "name")
    private String name;
    
    @OneToMany(mappedBy = "feed", fetch = FetchType.LAZY)
    private Set<FeedEntry> entries;

    public Feed() {
        entries = Collections.EMPTY_SET;
    }

    public Feed(String name) {
        this();
        
        this.name = name;
    }
    
    public Set<FeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<FeedEntry> entries) {
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
