package org.atomhopper.jdbc.model;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class PersistedEntry {

    private long sqlId;
    private String entryId;
    private String feed;
    private String entryBody;
    private Date creationDate;
    private Date dateLastUpdated;
    private String[] categories;

    public PersistedEntry() {
        final Calendar localNow = Calendar.getInstance(TimeZone.getDefault());
        localNow.setTimeInMillis(System.currentTimeMillis());

        creationDate = localNow.getTime();
        dateLastUpdated = creationDate;
    }

    public long getSqlId() {
        return sqlId;
    }

    public void setSqlId(long sqlId) {
        this.sqlId = sqlId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getFeed() {
        return feed;
    }

    public void setFeed(String feed) {
        this.feed = feed;
    }

    public String getEntryBody() {
        return entryBody;
    }

    public void setEntryBody(String entryBody) {
        this.entryBody = entryBody;
    }

    public Date getCreationDate() {
        return (Date) creationDate.clone();
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = (Date) creationDate.clone();
    }

    public Date getDateLastUpdated() {
        return (Date) dateLastUpdated.clone();
    }

    public void setDateLastUpdated(Date dateLastUpdated) {
        this.dateLastUpdated = (Date) dateLastUpdated.clone();
    }

    public String[] getCategories() {
        return categories.clone();
    }

    public void setCategories(String[] categories) {
        if (categories != null) {
            this.categories = Arrays.copyOf(categories, categories.length);
        }
    }
}
