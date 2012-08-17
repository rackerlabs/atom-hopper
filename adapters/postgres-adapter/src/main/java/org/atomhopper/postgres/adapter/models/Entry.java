package org.atomhopper.postgres.adapter.models;

import java.util.Date;


public class Entry {
    
    private String entryId;
    private String feedName;
    private String entryBody;
    private Date creationDate;
    private Date dateLastUpdated;
    private String[] categories;

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getFeedName() {
        return feedName;
    }

    public void setFeedName(String feedName) {
        this.feedName = feedName;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(Date dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }
}
