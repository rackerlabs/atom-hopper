package org.atomhopper.adapter.hibernate.impl.domain;

import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "feeds")
public class Feed {

    @Id
    @Column(name = "id")
    @GeneratedValue(generator = "increment")
    @GenericGenerator(name = "increment", strategy = "increment")
    private int id;
    
    @Column(name = "name")
    private String name;
    
    @OneToMany(mappedBy = "feed")
    private Set<FeedEntry> entries;

    public Set<FeedEntry> getEntries() {
        return entries;
    }

    public void setEntries(Set<FeedEntry> entries) {
        this.entries = entries;
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
