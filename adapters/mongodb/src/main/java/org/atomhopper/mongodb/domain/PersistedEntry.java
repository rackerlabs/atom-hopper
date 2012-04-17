package org.atomhopper.mongodb.domain;

import java.util.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "persistedentry")
public class PersistedEntry {

    @Id
    private String entryId;
    private String entryBody;
    private List<PersistedCategory> categories = new ArrayList<PersistedCategory>();
    private String feed;
    private Date creationDate;
    private Date dateLastUpdated;

    public PersistedEntry() {
        final Calendar localNow = Calendar.getInstance(TimeZone.getDefault());
        localNow.setTimeInMillis(System.currentTimeMillis());

        creationDate = localNow.getTime();
        dateLastUpdated = localNow.getTime();
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

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }
    /*
     * public List<PersistedCategory> getCategories() { return categories; }
     *
     * public void setCategories(List<PersistedCategory> categories) {
     * this.categories = categories; }
    *
     */

    public List<PersistedCategory> getCategories() {
        return categories;
    }

    public void addCategory(PersistedCategory persistedCategories) {

        this.categories.add(persistedCategories);
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    @Override
    public String toString() {
        return "";
        /*
         * return "PersistedEntry [id=" + this.entryId + ", entryBody=" +
         * this.entryBody + ", creationDate=" + this.creationDate + ",
         * dateLastupdated=" + this.dateLastUpdated + ", categories=" +
         * Arrays.toString(this.categories) + "]";
         */
    }
}