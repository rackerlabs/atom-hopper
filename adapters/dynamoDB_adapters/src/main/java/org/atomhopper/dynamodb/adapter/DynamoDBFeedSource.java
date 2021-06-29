package org.atomhopper.dynamodb.adapter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.*;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.atomhopper.dynamodb.query.SQLToNoSqlConverter;
import org.atomhopper.dynamodb.query.SearchType;
import org.atomhopper.dynamodb.query.DynamoDBQueryBuilder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author shub6691
 * Implements the DynamoDBFeedSource interface for retrieving feed entries from a datastore.  This class implements
 * the following:
 *
 * <ul>
 *     <li>Generating the feed entries from PersistedEntry instances</li>
 *     <li>Accessing data from a dynamodb table where all categories are treated equally</li>
 *     <li>Read categories with predefined prefixes from specified columns for better search performance</li>
 * </ul>
 * <p>
 * Mapping category prefixes to postgres columns is done through the following:
 * <ul>
 *     <li>PrefixColumnMap - maps a prefix key to a column name.  E.g., 'tid' to 'tenantid'</li>
 *     <li>Delimiter - used to extract the prefix from a category.  E.g., if the delimiter is ':' the category
 *     value would be 'tid:1234'</li>
 * </ul>
 */
public class DynamoDBFeedSource {

    static Logger LOG = LoggerFactory.getLogger(DynamoDBFeedSource.class);
    private AmazonDynamoDBClient amazonDynamoDBClient;
    private DynamoDB dynamoDB;
    private DynamoDBMapper mapper;
    private boolean enableTimers = false;
    private boolean enableLoggingOnShortPage = false;
    private int feedHeadDelayInSeconds = 2;

    private Map<String, String> mapPrefix = new HashMap<String, String>();
    private Map<String, String> mapColumn = new HashMap<String, String>();

    private String split;

    private AdapterHelper helper = new AdapterHelper();

    private SQLToNoSqlConverter getSearchToSqlConverter() {

        return new SQLToNoSqlConverter(mapPrefix, split);
    }
    public void setPrefixColumnMap(Map<String, String> prefix) {

        mapPrefix = new HashMap<String, String>(prefix);

        mapColumn = new HashMap<String, String>();

        for (String key : mapPrefix.keySet()) {

            mapColumn.put(mapPrefix.get(key), key);
        }
    }

    public void setDelimiter(String splitParam) {

        split = splitParam;
    }

    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void afterPropertiesSet() {

        if (split != null ^ !mapPrefix.isEmpty()) {

            throw new IllegalArgumentException("The 'delimiter' and 'prefixColumnMap' field must both be defined");
        }
    }


    public DynamoDBFeedSource(AmazonDynamoDBClient amazonDynamoDBClient, DynamoDBMapper mapper) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.dynamoDB = new DynamoDB(this.amazonDynamoDBClient);
        this.mapper = mapper;
        setDynamoDB(dynamoDB);
    }

    public void setDynamoDB(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    /**
     * This method is used to return the categories based search with the help of feed and entryId and search type is backward
     * whch means it will search for date less the markerDate along with other params.
     * @param feedName: Name of the feed for each entry . For ex: namespace/feed
     * @param markerTimestamp: Timestamp for which the search is to be performed for category
     * @param markerId: EntryID for every event.
     * @param searchString: The string on which search is performed in db
     * @param pageSize: default size is 25
     * @return List of PersistedEntry data found based on above params from db.
     */
    public List<PersistedEntry> getFeedBackward(String feedName,
                                                Date markerTimestamp,
                                                long markerId,
                                                String searchString,
                                                int pageSize) {
        List<PersistedEntry> feedPage;
        DynamoDBQueryBuilder sqlBac = new DynamoDBQueryBuilder(getSearchToSqlConverter()).searchString(searchString);
        sqlBac.searchType(SearchType.FEED_BACKWARD);
        //Dynamodb query implementation
        Map<String, String> map = new HashMap<String, String>();
        String filters = sqlBac.getFilters(map);
        String feedNameFilter=" and feed= :feedName";
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String dateLastUpdated = dateFormatter.format(markerTimestamp);
        Map<String, AttributeValue> valueMap = new HashMap<String, AttributeValue>();
        valueMap.put(":id", new AttributeValue().withS(String.valueOf(markerId)));
        valueMap.put(":dateLastUpdated", new AttributeValue().withS(dateLastUpdated));
        valueMap.put(":feedName",new AttributeValue().withS(feedName));
        for (Map.Entry<String, String> res : map.entrySet()) {
            valueMap.put(res.getKey(), new AttributeValue().withS(res.getValue()));
        }
        DynamoDBQueryExpression<PersistedEntry> querySpec = new DynamoDBQueryExpression()
                .withKeyConditionExpression("entryId = :id and dateLastUpdated <= :dateLastUpdated")
                .withScanIndexForward(false)
                .withLimit(pageSize)
                .withFilterExpression(filters + feedNameFilter)
                .withExpressionAttributeValues(valueMap);
        feedPage = mapper.query(PersistedEntry.class, querySpec);
        return feedPage;
    }

    /**
     * This method is used to return list of entries from DynamoDb bases on feedName and timestamp.
     * @param getFeedRequest: It contains the request object parameters
     * @param startingAt: The timestamp from where we need to perform search
     * @param pageSize: for pagination . Default page size is 25
     * @return List of entries found based on given timestamp ,feed name.
     */
    //TODO LINK REF NEED TO BE IMPLEMENTED FOR HYDRATED FEED.
    public List<String> getFeedPageByTimestamp(GetFeedRequest getFeedRequest, String startingAt, int pageSize) throws Exception {
        final String pageDirectionValue = getFeedRequest.getDirection();
        PageDirection pageDirection = PageDirection.FORWARD;
        if (StringUtils.isNotEmpty(String.valueOf(pageDirection))) {
            pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());
        }
        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
        DateTimeFormatter isoDTF = ISODateTimeFormat.dateTime();
        DateTime startAt = isoDTF.parseDateTime(startingAt);
        List<String> entryMarker = getEntryByTimestamp(startAt, getFeedRequest.getFeedName(), pageDirection);
        if (entryMarker.isEmpty()) {
            throw new RuntimeException("No entry with specified startingAt timestamp found");
        }
        return entryMarker;
    }

    /**
     * This method creates the query for fetching the data based on timestamp and the direction from DynamoDB
     * @param
     * @param markerDate: StartAt Must be in ISO 8601 Date and Time format, and must contain a time zone,
     *                  for example: 2014-03-10T06:00:00.000Z. For more information, see ISO 8601 Date and Time format.
     * @param direction: Specifies the direction from which to return entries, starting from the current marker or entry.
     *                 Can be either forward or backward.
     * @return List of data present based on the search query.
     */
    protected List<String> getEntryByTimestamp(final DateTime markerDate, final String feed, PageDirection direction) {
        List<String> feedPage = new ArrayList<String>();
        Table table = dynamoDB.getTable("entries");
        Index index = table.getIndex("global-feed-index");
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("feed = :feed and " + getTimeStampValueFilter(direction))
                .withValueMap(new ValueMap()
                        .withString(":feed", feed)
                        .withString(":markerDate", String.valueOf(markerDate)));
        ItemCollection<QueryOutcome> persistedEntryItems = index.query(spec);
        Iterator<Item> itemsIterator = persistedEntryItems.iterator();
        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next();
            feedPage.add(item.toJSONPretty());
        }
        return feedPage;
    }

    /**
     *This method is used to create a query for dynamodb for timestamp search based on direction
     * @param direction: If the startingAt parameter is used without a direction parameter, then the forward direction is assumed.
     * If you want to fetch feeds from a time period before the time specified in the time stamp,
     *  you need to use the direction parameter and then the backward description, like the following: direction set to backward.
     * @return : the formed query based upon the direction.
     */
    private String getTimeStampValueFilter(PageDirection direction) {
        if (direction.equals(PageDirection.BACKWARD)) {
            return "dateLastUpdated <= :markerDate";
        } else {
            return "dateLastUpdated >= :markerDate";
        }
    }
}
