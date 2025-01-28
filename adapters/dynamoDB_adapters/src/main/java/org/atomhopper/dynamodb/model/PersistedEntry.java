package org.atomhopper.dynamodb.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import org.atomhopper.dynamodb.constant.DynamoDBConstant;

import java.util.*;


@DynamoDBTable(tableName = DynamoDBConstant.ENTRIES)
public class PersistedEntry {

    @DynamoDBHashKey(attributeName = DynamoDBConstant.ENTRY_ID)
    private String entryId;
    @DynamoDBIndexRangeKey(attributeName = DynamoDBConstant.FEED, localSecondaryIndexName = "entryId-feed-index")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "global-feed-index", attributeName = DynamoDBConstant.FEED)
    private String feed;
    private String entryBody;
    @DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
    private String creationDate;
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "global-feed-index", attributeName = DynamoDBConstant.DATE_LAST_UPDATED)
    @DynamoDBRangeKey(attributeName = DynamoDBConstant.DATE_LAST_UPDATED)
    private String dateLastUpdated;
    private List<String> categories;

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

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getDateLastUpdated() {
        return dateLastUpdated;
    }

    public void setDateLastUpdated(String dateLastUpdated) {
        this.dateLastUpdated = dateLastUpdated;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }


}
