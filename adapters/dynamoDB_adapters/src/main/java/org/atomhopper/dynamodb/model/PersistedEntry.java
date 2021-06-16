package org.atomhopper.dynamodb.model;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.Data;
import java.util.*;

@Data
@DynamoDBTable(tableName = "entries")
public class PersistedEntry {

    @DynamoDBHashKey(attributeName = "entryId")
    private String entryId;
    @DynamoDBIndexRangeKey(attributeName = "feed",
            localSecondaryIndexName = "entryId-feed-index")
    private String feed;
    private String entryBody;
    @DynamoDBAutoGeneratedTimestamp(strategy = DynamoDBAutoGenerateStrategy.CREATE)
    private String creationDate;
    @DynamoDBRangeKey(attributeName = "dateLastUpdated")
    private String dateLastUpdated;
    private List<String> categories;
}
