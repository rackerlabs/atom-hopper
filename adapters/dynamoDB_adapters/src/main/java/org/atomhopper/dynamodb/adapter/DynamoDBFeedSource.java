package org.atomhopper.dynamodb.adapter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.TimerContext;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.*;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.dynamodb.constant.DynamoDBConstant;
import org.atomhopper.dynamodb.model.PersistedEntry;
import org.atomhopper.dynamodb.query.JsonUtil;
import org.atomhopper.dynamodb.query.SQLToNoSqlConverter;
import org.atomhopper.dynamodb.query.SearchType;
import org.atomhopper.dynamodb.query.DynamoDBQueryBuilder;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;

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
public class DynamoDBFeedSource implements FeedSource {

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

    public Boolean getEnableLoggingOnShortPage() {
        return enableLoggingOnShortPage;
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


    public DynamoDBFeedSource(AmazonDynamoDBClient amazonDynamoDBClient) {
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        this.dynamoDB = new DynamoDB(this.amazonDynamoDBClient);
        this.mapper = new DynamoDBMapper(amazonDynamoDBClient);
        setDynamoDB(dynamoDB);
    }

    public void setDynamoDB(DynamoDB dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    /**
     * This method is used to return the categories based search with the help of feed and entryId and search type is backward
     * whch means it will search for date less the markerDate along with other params.
     *
     * @param feedName:        Name of the feed for each entry . For ex: namespace/feed
     * @param markerTimestamp: Timestamp for which the search is to be performed for category
     * @param markerId:        EntryID for every event.
     * @param searchString:    The string on which search is performed in db
     * @param pageSize:        default size is 25
     * @return List of PersistedEntry data found based on above params from db.
     */
    public List<PersistedEntry> getFeedBackward(String feedName,
                                                String markerTimestamp,
                                                String markerId,
                                                String searchString,
                                                int pageSize) {                                           
        List<PersistedEntry> feedPage;
        DynamoDBQueryBuilder sqlBac = new DynamoDBQueryBuilder(getSearchToSqlConverter()).searchString(searchString);
        sqlBac.searchType(SearchType.FEED_BACKWARD);
        //Dynamodb query implementation
        Map<String, String> map = new HashMap<String, String>();
        String filters = sqlBac.getFilters(map);

        if(null != filters){
            ValueMap valueMap2 = new ValueMap();
            valueMap2.withString(":feed", feedName);
            valueMap2.withString(":dateLastUpdated", markerTimestamp);
            String feedNameFilter = "dateLastUpdated <= :dateLastUpdated";
            for (Map.Entry<String, String> res : map.entrySet()) {
                valueMap2.withString(res.getKey(), res.getValue());
            }
            LOG.error("filterString: " + feedNameFilter + filters.substring(1, filters.length()));
            List<String> result = getQueryBuilderMethod(dynamoDB, "feed = :feed and " + getTimeStampValueFilter(PageDirection.BACKWARD) , filters.substring(1, filters.length()), pageSize,valueMap2, true);
            feedPage = JsonUtil.getPersistenceEntity(result);
        }else{
            ValueMap valueMap2 = new ValueMap();
            valueMap2.withString(":feed", feedName);
            valueMap2.withString(":dateLastUpdated", markerTimestamp);
            String feedNameFilter = "dateLastUpdated <= :dateLastUpdated";
            LOG.error("filterString: " + feedNameFilter + filters);
            List<String> result = getQueryBuilderMethod(dynamoDB, "feed = :feed and " + getTimeStampValueFilter(PageDirection.BACKWARD) ,null, pageSize,valueMap2, true);
            feedPage = JsonUtil.getPersistenceEntity(result);
        }     
        return feedPage;
    }


    /**
     * This method is used to return the categories based search with the help of feed and entryId and search type is forward
     * whch means it will search for date less the markerDate along with other params.
     *
     * @param feedName:        Name of the feed for each entry . For ex: namespace/feed
     * @param markerTimestamp: Timestamp for which the search is to be performed for category
     * @param markerId:        EntryID for every event.
     * @param searchString:    The string on which search is performed in db
     * @param pageSize:        default size is 25
     * @return List of PersistedEntry data found based on above params from db.
     */
    public List<PersistedEntry> getFeedForward(String feedName,
                                               String markerTimestamp,
                                               String markerId,
                                               String searchString,
                                               int pageSize) {
        List<PersistedEntry> feedPage;
        DynamoDBQueryBuilder sqlBac = new DynamoDBQueryBuilder(getSearchToSqlConverter()).searchString(searchString);
        sqlBac.searchType(SearchType.FEED_FORWARD);
        //Dynamodb query implementation
        Map<String, String> map = new HashMap<String, String>();
        String filters = sqlBac.getFilters(map);
        if(null != filters){
            ValueMap valueMap2 = new ValueMap();
            valueMap2.withString(":feed", feedName);
            valueMap2.withString(":dateLastUpdated", markerTimestamp);
            String feedNameFilter = "dateLastUpdated >= :dateLastUpdated";
            for (Map.Entry<String, String> res : map.entrySet()) {
                valueMap2.withString(res.getKey(), res.getValue());
            }
            List<String> result = getQueryBuilderMethod(dynamoDB, "feed = :feed and " + "dateLastUpdated >= :dateLastUpdated" , filters.substring(1, filters.length()), pageSize,valueMap2, true);
            feedPage = JsonUtil.getPersistenceEntity(result);
        }else{
            ValueMap valueMap2 = new ValueMap();
            valueMap2.withString(":feed", feedName);
            valueMap2.withString(":dateLastUpdated", markerTimestamp);
            List<String> result = getQueryBuilderMethod(dynamoDB, "feed = :feed and " + "dateLastUpdated >= :dateLastUpdated" , null, pageSize,valueMap2, true);
            feedPage = JsonUtil.getPersistenceEntity(result);
        }

        return feedPage;
    }

    /**
     * This method is used to return list of entries from DynamoDb bases on feedName and timestamp.
     *
     * @param getFeedRequest : It contains the request object parameters
     * @param startingAt     :     The timestamp from where we need to perform search
     * @param pageSize       :       for pagination . Default page size is 25
     * @return List of entries found based on given timestamp ,feed name.
     */
    //TODO LINK REF NEED TO BE IMPLEMENTED FOR HYDRATED FEED.
    public AdapterResponse<Feed> getFeedPageByTimestamp(GetFeedRequest getFeedRequest, String startingAt, int pageSize) throws Exception {
        final String pageDirectionValue = getFeedRequest.getDirection();
        ObjectMapper mapper = new ObjectMapper();
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
        // This list contains the persistent object in json string format
        PersistedEntry persistedEntry = mapper.readValue(entryMarker.get(0), PersistedEntry.class);
        //Convert String to Date Format
        String lastDateUpdated = persistedEntry.getDateLastUpdated();
        final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                enhancedGetFeedPage(getFeedRequest.getFeedName(),
                        lastDateUpdated,
                        persistedEntry.getEntryId(),
                        pageDirection,
                        searchString, pageSize),
                getFeedRequest, pageSize); 
        return ResponseBuilder.found(feed);
    }


    /**
     * This method creates the query for fetching the data based on timestamp and the direction from DynamoDB
     *
     * @param
     * @param markerDate: StartAt Must be in ISO 8601 Date and Time format, and must contain a time zone,
     *                    for example: 2014-03-10T06:00:00.000Z. For more information, see ISO 8601 Date and Time format.
     * @param direction:  Specifies the direction from which to return entries, starting from the current marker or entry.
     *                    Can be either forward or backward.
     * @return List of data present based on the search query.
     */
    protected List<String> getEntryByTimestamp(final DateTime markerDate, final String feed, PageDirection direction) {
        ValueMap valueMap = new ValueMap();
        valueMap.withString(":feed", feed);
        valueMap.withString(":dateLastUpdated", String.valueOf(markerDate));
        return getQueryBuilderMethod(dynamoDB, "feed = :feed and " + getTimeStampValueFilter(direction) , null, valueMap);
    }

    /**
     * This method is used to create a query for dynamodb for timestamp search based on direction
     *
     * @param direction: If the startingAt parameter is used without a direction parameter, then the forward direction is assumed.
     *                   If you want to fetch feeds from a time period before the time specified in the time stamp,
     *                   you need to use the direction parameter and then the backward description, like the following: direction set to backward.
     * @return : the formed query based upon the direction.
     */
    private String getTimeStampValueFilter(PageDirection direction) {
        if (direction.equals(PageDirection.BACKWARD)) {
            return "dateLastUpdated <= :dateLastUpdated";
        } else {
            return "dateLastUpdated > :dateLastUpdated";
        }
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This method returns the search result based on the type of operation like search getFeedHead,getFeedPageByTimestamp ect
     *
     * @param getFeedRequest: all the request type properties mentioned below ;
     *                        List<String> getCategories();
     *                        String getSearchQuery();
     *                        String getPageMarker();
     *                        String getPageSize();
     *                        String getDirection();
     *                        String getStartingAt();
     * @return
     */
    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        AdapterResponse<Feed> response = null;
        TimerContext context = null;
        int pageSize = DynamoDBConstant.PAGE_SIZE;
        final String pageSizeString = getFeedRequest.getPageSize();
        if (StringUtils.isNotBlank(pageSizeString)) {
            pageSize = Integer.parseInt(pageSizeString);
        }
        final String marker = getFeedRequest.getPageMarker();
        final String startingAt = getFeedRequest.getStartingAt();
        if (StringUtils.isNotBlank(marker) && StringUtils.isNotBlank(startingAt)) {
            response = ResponseBuilder.badRequest("'marker' parameter can not be used together with the 'startingAt' parameter");
            return response;
        }

        try {

            if (StringUtils.isBlank(marker) && StringUtils.isBlank(startingAt)) {

                context = startTimer(String.format("get-feed-head-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedHead(getFeedRequest, pageSize);
            } else if (StringUtils.isNotBlank(marker) && marker.equals(DynamoDBConstant.MOCK_LAST_MARKER)) {

                context = startTimer(String.format("get-last-page-%s", getMetricBucketForPageSize(pageSize)));
                response = getLastPage(getFeedRequest, pageSize);
            } else if (StringUtils.isNotBlank(marker)) {
                context = startTimer(String.format("get-feed-page-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedPage(getFeedRequest, marker, pageSize);
            } else {
                // we process 'startingAt' parameter here
                context = startTimer(String.format("get-feed-page-startingAt-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedPageByTimestamp(getFeedRequest, startingAt, pageSize);
            }
        } catch (IllegalArgumentException iae) {
            response = ResponseBuilder.badRequest(iae.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopTimer(context);
        }

        return response;
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        final List<PersistedEntry> entry = getEntry(getEntryRequest.getEntryId(), getEntryRequest.getFeedName());

        AdapterResponse<Entry> response = ResponseBuilder.notFound();

        if (!entry.isEmpty()) {
            response = ResponseBuilder.found(hydrateEntry(entry.get(0), getEntryRequest.getAbdera()));
        }
        return response;
    }

    /**
     * This method returns the feed page based on the entryId for every event
     *
     * @param getFeedRequest
     * @param marker:        entry id fo every feed
     * @param pageSize:      default is 25,for pagination
     * @throws ParseException
     * @return: Response as a Http response
     */

    private AdapterResponse<Feed> getFeedPage(GetFeedRequest getFeedRequest, String marker, int pageSize) throws ParseException {
        final String pageDirectionValue = getFeedRequest.getDirection();
        PageDirection pageDirection = PageDirection.FORWARD;
        if (StringUtils.isNotEmpty(pageDirectionValue)) {
            pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());
        }
        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
        List<PersistedEntry> entryMarker = getEntry(marker, getFeedRequest.getFeedName());
        if (entryMarker.isEmpty()) {
            return ResponseBuilder.notFound("No entry with specified marker found");
        }
        String lastDateUpdated = entryMarker.get(0).getDateLastUpdated();
        final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                enhancedGetFeedPage(getFeedRequest.getFeedName(),
                        lastDateUpdated,
                        entryMarker.get(0).getEntryId(),
                        pageDirection,
                        searchString, pageSize),
                getFeedRequest, pageSize);
        return ResponseBuilder.found(feed);
    }

    /**
     * This method is used to return the feed based on direction weather is a forward or backward
     *
     * @param feedName:        name of feed
     * @param markerTimestamp: for which time stamp it need to be searched
     * @param markerId:        entry id for every feed
     * @param direction:       it can be forward or backward
     * @param searchString:    category search string
     * @param pageSize:        default is 25 ,used for pagination
     * @return: list of PersistedEntry object .
     */
    private List<PersistedEntry> enhancedGetFeedPage(final String feedName, final String markerTimestamp,
                                                     final String markerId,
                                                     final PageDirection direction, final String searchString,
                                                     final int pageSize) {
        List<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();

        TimerContext context = null;

        boolean hasCats = !searchString.trim().isEmpty();

        try {
            switch (direction) {
                case FORWARD:


                    if (hasCats) {
                        context = startTimer(String.format("db-get-feed-page-forward-with-cats-%s",
                                getMetricBucketForPageSize(pageSize)));
                    } else {
                        context = startTimer(
                                String.format("db-get-feed-page-forward-%s", getMetricBucketForPageSize(pageSize)));
                    }
                    feedPage = getFeedForward(feedName,
                            markerTimestamp,
                            markerId,
                            searchString,
                            pageSize);

                    Collections.reverse(feedPage);
                    break;

                case BACKWARD:


                    if (hasCats) {
                        context = startTimer(String.format("db-get-feed-page-backward-with-cats-%s",
                                getMetricBucketForPageSize(pageSize)));
                    } else {
                        context = startTimer(
                                String.format("db-get-feed-page-backward-%s", getMetricBucketForPageSize(pageSize)));
                    }
                    feedPage = getFeedBackward(feedName,
                            markerTimestamp,
                            markerId,
                            searchString,
                            pageSize);
                    break;
            }
        } finally {
            stopTimer(context);
        }
        return feedPage;
    }

    /**
     * This method is used to return the last page of the particular feed based on entryID
     *
     * @param getFeedRequest: Its has all required feed request objects
     * @param pageSize:       Page size for pagination ,default size is 25
     * @return
     */

    private AdapterResponse<Feed> getLastPage(GetFeedRequest getFeedRequest, int pageSize) {

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
        AdapterResponse<Feed> response;

        final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                enhancedGetLastPage(getFeedRequest.getFeedName(), pageSize, searchString),
                getFeedRequest, pageSize);
        response = ResponseBuilder.found(feed);

        return response;
    }

    /**
     * This method returns the Last Page of the feed by performing union of both the select statements output
     *
     * @param feedName:     FeedName
     * @param pageSize:     for pagination
     * @param searchString: Search Category to be passed
     * @return List of persistent Object for union results
     */
    private List<PersistedEntry> enhancedGetLastPage(final String feedName, final int pageSize, final String searchString) {

        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString(searchString);
        int numCats = categoriesList.size();
        int counter = numCats;
                                                                                              
        String filterExpression = null;
        if (counter > 0) {
            filterExpression = "(contains(categories, :categories"+ (numCats - counter)+")";
            counter--;
            while(counter > 0){
                filterExpression = filterExpression + " and contains(categories, :categories"+ (numCats - counter)+")";
                counter--;
            }
            filterExpression = filterExpression + ")";
            }
                                                        
            TimerContext context = null;
        try {
            if (numCats > 0) {
                context = startTimer(
                        String.format("db-get-last-page-with-cats-%s", getMetricBucketForPageSize(pageSize)));
            } else {
                context = startTimer(String.format("db-get-last-page-%s", getMetricBucketForPageSize(pageSize)));
            }

            List<String> feedPage;
            ValueMap valueMap = new ValueMap();
            valueMap.withString(":feed", feedName);
            for(int i = 0; i < categoriesList.size() ; i++){
                String s = categoriesList.get(i);
                if(s.charAt(0) == '{'){
                    valueMap.withString(":categories"+i, s.substring(1,s.length() -1));
                }else{
                    valueMap.withString(":categories"+i, s);
                }
            }
            feedPage = getQueryBuilderMethod(dynamoDB, "feed = :feed",filterExpression, pageSize, valueMap, true);
            List<PersistedEntry> persistedEntryList = JsonUtil.getPersistenceEntity(feedPage);
            return persistedEntryList;
        } finally {
            stopTimer(context);
        }
    }

    /**
     * @param getFeedRequest
     * @param pageSize
     * @return
     */
    private AdapterResponse<Feed> getFeedHead(GetFeedRequest getFeedRequest, int pageSize) {
        final Abdera abdera = getFeedRequest.getAbdera();

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        List<PersistedEntry> persistedEntries = getFeedHead(getFeedRequest.getFeedName(), pageSize, searchString);

        Feed hydratedFeed = hydrateFeed(abdera, persistedEntries, getFeedRequest, pageSize);

        // Set the last link in the feed head
        final String baseFeedUri = decode(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));

        if (!helper.isArchived()) {

            hydratedFeed.addLink(
                            new StringBuilder().append(baseFeedUri)
                                    .append(DynamoDBConstant.MARKER_EQ).append(DynamoDBConstant.MOCK_LAST_MARKER)
                                    .append(DynamoDBConstant.AND_LIMIT_EQ).append(String.valueOf(pageSize))
                                    .append(DynamoDBConstant.AND_SEARCH_EQ).append(urlEncode(searchString))
                                    .append(DynamoDBConstant.AND_DIRECTION_EQ_BACKWARD).toString())
                    .setRel(Link.REL_LAST);
        }

        return ResponseBuilder.found(hydratedFeed);

    }

    private String getMetricBucketForPageSize(int pageSize) {
        if (pageSize > 0 && pageSize <= 249) {
            return "tiny";
        } else if (pageSize >= 250 && pageSize <= 499) {
            return "small";
        } else if (pageSize >= 500 && pageSize <= 749) {
            return "medium";
        } else {
            // 750 - 1000
            return "large";
        }
    }

    /**
     * This method is used to return the feed head which means the starting feed for the particular entry
     *
     * @param feedName:     name of the feed
     * @param pageSize:     default is 25, for pagination
     * @param searchString: Search string to be used to filteration in the query
     * @return List of all the output of PersistedEntry
     */
    private List<PersistedEntry> getFeedHead(final String feedName, final int pageSize, final String searchString) {
        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString(searchString);
        int numCats = categoriesList.size();
        int counter = numCats;
        
        String filterExpression = null;

        if (counter > 0) {
            filterExpression = "(contains(categories, :categories"+ (numCats - counter)+")";
            counter--;
            while(counter > 0){
                filterExpression = filterExpression + " and contains(categories, :categories"+ (numCats - counter)+")";
                counter--;
            }
            filterExpression = filterExpression + ")";
        }
        

        TimerContext context = null;
        try {
            if (numCats > 0) {
                context = startTimer(
                        String.format("db-get-feed-head-with-cats-%s", getMetricBucketForPageSize(pageSize)));
            } else {
                context = startTimer(String.format("db-get-feed-head-%s", getMetricBucketForPageSize(pageSize)));
            }

            List<String> feedPage;
            ValueMap valueMap = new ValueMap();
            valueMap.withString(":feed", feedName);
            for(int i = 0; i < categoriesList.size() ; i++){
                String s = categoriesList.get(i);
                if(s.charAt(0) == '{'){
                    valueMap.withString(":categories"+i, s.substring(1,s.length() -1));
                }else{
                    valueMap.withString(":categories"+i, s);
                }
            }
            feedPage = getQueryBuilderMethod(dynamoDB, "feed = :feed",filterExpression, pageSize, valueMap, false);
            List<PersistedEntry> persistedEntryList = JsonUtil.getPersistenceEntity(feedPage);
            return persistedEntryList;
        } finally {
            stopTimer(context);
        }
    }

    @Override
    public void setCurrentUrl(URL urlCurrent) {
        helper.setCurrentUrl(urlCurrent);
    }

    @Override
    public void setArchiveUrl(URL url) {
        helper.setArchiveUrl(url);
    }

    private TimerContext startTimer(String name) {
        if (enableTimers) {
            final com.yammer.metrics.core.Timer timer = Metrics.newTimer(getClass(), name, TimeUnit.MILLISECONDS,
                    TimeUnit.SECONDS);
            TimerContext context = timer.time();
            return context;
        } else {
            return null;
        }


    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries,
                             GetFeedRequest getFeedRequest, final int pageSize) {                     
        final Feed hydratedFeed = abdera.newFeed();
        final String baseFeedUri = decode(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));
        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
        if (helper.isArchived()) {

            helper.addArchiveNode(hydratedFeed);
        }

        // Set the feed links
        addFeedCurrentLink(hydratedFeed, baseFeedUri);
        addFeedSelfLink(hydratedFeed, baseFeedUri, getFeedRequest, pageSize, searchString);


        PersistedEntry nextEntry = null;

        // TODO: We should have a link builder method for these
        if (!(persistedEntries.isEmpty())) {
            hydratedFeed.setId(DynamoDBConstant.UUID_URI_SCHEME + UUID.randomUUID().toString());
            hydratedFeed.setTitle(persistedEntries.get(0).getFeed());

            // Set the previous link
            hydratedFeed.addLink(new StringBuilder()
                            .append(baseFeedUri).append(DynamoDBConstant.MARKER_EQ)
                            .append(persistedEntries.get(0).getEntryId())
                            .append(DynamoDBConstant.AND_LIMIT_EQ).append(String.valueOf(pageSize))
                            .append(DynamoDBConstant.AND_SEARCH_EQ).append(urlEncode(searchString))
                            .append(DynamoDBConstant.AND_DIRECTION_EQ_FORWARD).toString())
                    .setRel(helper.getPrevLink());

            final PersistedEntry lastEntryInCollection = persistedEntries.get(persistedEntries.size() - 1);

            nextEntry = getNextMarker(lastEntryInCollection, getFeedRequest.getFeedName(), searchString);

            if (nextEntry != null) {
                // Set the next link
                hydratedFeed.addLink(new StringBuilder().append(baseFeedUri)
                                .append(DynamoDBConstant.MARKER_EQ).append(nextEntry.getEntryId())
                                .append(DynamoDBConstant.AND_LIMIT_EQ).append(String.valueOf(pageSize))
                                .append(DynamoDBConstant.AND_SEARCH_EQ).append(urlEncode(searchString))
                                .append(DynamoDBConstant.AND_DIRECTION_EQ_BACKWARD).toString())
                        .setRel(helper.getNextLink());
            }
        }

        if (nextEntry == null && helper.getArchiveUrl() != null) {
            hydratedFeed.addLink(new StringBuilder().append(helper.getArchiveUrl()).append(DynamoDBConstant.LIMIT_EQ).append(String.valueOf(pageSize))
                            .append(DynamoDBConstant.AND_DIRECTION_EQ_BACKWARD).toString())
                    .setRel(FeedSource.REL_ARCHIVE_NEXT);
        }

        for (PersistedEntry persistedFeedEntry : persistedEntries) {
            hydratedFeed.addEntry(hydrateEntry(persistedFeedEntry, abdera));
        }

        if (getEnableLoggingOnShortPage()) {
            if (hydratedFeed.getEntries() != null && hydratedFeed.getEntries().size() < pageSize) {
                LOG.warn("User requested " + getFeedRequest.getFeedName() + " feed with limit " + pageSize + ", but returning only " + hydratedFeed.getEntries().size());
                List<Entry> entries = hydratedFeed.getEntries();
                StringBuilder sb = new StringBuilder();
                for (int idx = 0; idx < entries.size(); idx++) {
                    Entry entry = entries.get(idx);
                    sb.append(entry.getId() + ", ");
                }
                LOG.warn("UUIDs: " + sb.toString());
            } else if (hydratedFeed.getEntries() == null) {
                LOG.warn("User requested " + getFeedRequest.getFeedName() + " feed with limit " + pageSize + ", but no entries are available");
            }
        }

        return hydratedFeed;
    }

    /**
     * This method is used to get the next marker based for the markerID and the feedName
     *
     * @param persistedEntry: PersistentEntryModel
     * @param feedName:       name of the feed
     * @param searchString:   Category search string for filteration
     * @return Persistent model Object
     */
    private PersistedEntry getNextMarker(final PersistedEntry persistedEntry, final String feedName,
                                         final String searchString) {

        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString(searchString);
        int numCats = categoriesList.size();
        int counter = numCats;                               
        String filterExpression = null;

        if (counter > 0) {
            filterExpression = "(contains(categories, :categories"+ (numCats - counter)+")";
            counter--;
            while(counter > 0){
                filterExpression = filterExpression + " and contains(categories, :categories"+ (numCats - counter)+")";
                counter--;
            }
            filterExpression = filterExpression + ")";
        }



        List<String> firstUnionPersistentList;
        ValueMap valueMap = new ValueMap();
        valueMap.withString(":feed", persistedEntry.getFeed());

        for(int i = 0; i < categoriesList.size() ; i++){
            String s = categoriesList.get(i);
            if(s.charAt(0) == '{'){
                valueMap.withString(":categories"+i, s.substring(1,s.length() -1));
            }else{
                valueMap.withString(":categories"+i, s);
            }
        }

        firstUnionPersistentList = getQueryBuilderMethod(dynamoDB, "feed = :feed ",filterExpression, valueMap);
        List<PersistedEntry> persistedEntryList = JsonUtil.getPersistenceEntity(firstUnionPersistentList);
        return persistedEntryList.get(0);
    }


    private void addFeedSelfLink(Feed feed, String baseFeedUri, GetFeedRequest getFeedRequest, int pageSize, String searchString) {
        StringBuilder queryParams = new StringBuilder();
        boolean markerIsSet = false;

        queryParams.append(baseFeedUri).append(DynamoDBConstant.LIMIT_EQ).append(
                String.valueOf(pageSize));

        if (searchString.length() > 0) {
            queryParams.append(DynamoDBConstant.AND_SEARCH_EQ).append(urlEncode(searchString));
        }
        if (getFeedRequest.getPageMarker() != null && getFeedRequest.getPageMarker().length() > 0) {
            queryParams.append(DynamoDBConstant.AND_MARKER_EQ).append(getFeedRequest.getPageMarker());
            markerIsSet = true;
        }
        if (markerIsSet) {
            queryParams.append(DynamoDBConstant.AND_DIRECTION_EQ).append(getFeedRequest.getDirection());
        } else {
            queryParams.append(DynamoDBConstant.AND_DIRECTION_EQ_BACKWARD);
            if (queryParams.toString().equalsIgnoreCase(
                    baseFeedUri + DynamoDBConstant.LIMIT_EQ + "25" + DynamoDBConstant.AND_DIRECTION_EQ_BACKWARD)) {
                // They are calling the feedhead, just use the base feed uri
                // This keeps the validator at http://validator.w3.org/ happy
                queryParams.delete(0, queryParams.toString().length()).append(
                        baseFeedUri);
            }
        }
        feed.addLink(queryParams.toString()).setRel(Link.REL_SELF);
    }


    private void addFeedCurrentLink(Feed hydratedFeed, String baseFeedUri) {
        String url = helper.isArchived() ? helper.getCurrentUrl() : baseFeedUri;

        hydratedFeed.addLink(url, Link.REL_CURRENT);
    }


    private Entry hydrateEntry(PersistedEntry persistedEntry, Abdera abderaReference) {

        final Document<Entry> hydratedEntryDocument = abderaReference.getParser().parse(
                new StringReader(persistedEntry.getEntryBody()));

        Entry entry = null;

        if (hydratedEntryDocument != null) {
            entry = hydratedEntryDocument.getRoot();
            entry.setUpdated(persistedEntry.getDateLastUpdated());
            entry.setPublished(persistedEntry.getCreationDate());
        }

        return entry;
    }

    /**
     * @param context
     */
    private void stopTimer(TimerContext context) {
        if (enableTimers && context != null) {
            context.stop();
        }
    }

    /**
     * @param searchString
     * @return
     */
    private String urlEncode(String searchString) {
        try {
            return URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //noop - should never get here
            return "";
        }
    }

    /**
     * To get the entry from dynamodb based upon two params?
     *
     * @param entryId:  It is the marker id for entry for every events
     * @param feedName: feed name is used to search the records in dynamodb
     * @return : list of entry found if that exits in dynamodb with the entryId and feedName.
     */
    private List<PersistedEntry> getEntry(String entryId, final String feedName) {
        Gson gson = new Gson();
        List<PersistedEntry> persistedEntriesObject = new ArrayList<PersistedEntry>();
        Table table = dynamoDB.getTable(DynamoDBConstant.ENTRIES);
        Index index = table.getIndex(DynamoDBConstant.ENTRY_ID_FEED_INDEX);
        QuerySpec spec = new QuerySpec()
                .withKeyConditionExpression("entryId = :entryId and feed = :feed")
                .withValueMap(new ValueMap()
                        .withString(":entryId", entryId)
                        .withString(":feed", feedName));
        ItemCollection<QueryOutcome> persistedEntryItems = index.query(spec);
        Iterator<Item> itemsIterator = persistedEntryItems.iterator();
        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next();
            persistedEntriesObject.add(gson.fromJson(item.toJSONPretty(), PersistedEntry.class));
        }
        return persistedEntriesObject;
    }

    /**
     * This method is used to build a query based on the condition expression(for ex with where clause)and other params
     *
     * @param dynamoDB:            object of dynamoDb
     * @param conditionExpression: Filtered Exppression based on which results are filtered(where clause conditions)
     * @param pageSize:            page size for pagination
     * @param valueMap:            map for keeping the mapped  values passed in a condition expression
     * @return List of String in json format.
     */
    public List<String> getQueryBuilderMethod(DynamoDB dynamoDB, String conditionExpression,String filterExpression,  int pageSize, ValueMap valueMap, boolean orderBy) {
        List<String> feedPage = new ArrayList<>();
        Table table = dynamoDB.getTable("entries");
        Index index = table.getIndex("global-feed-index");
        QuerySpec spec = null;
        if(null!= filterExpression){
            spec = new QuerySpec()
                .withKeyConditionExpression(conditionExpression)
                .withFilterExpression(filterExpression)
                .withValueMap(valueMap)
                .withMaxResultSize(pageSize)// for no of page limit to be displayed
                .withScanIndexForward(orderBy);
        }else{
            spec = new QuerySpec()
            .withKeyConditionExpression(conditionExpression)
            .withValueMap(valueMap)
            .withMaxResultSize(pageSize)// for no of page limit to be displayed
            .withScanIndexForward(orderBy);
        }
        // for descending order sorting on lastDateUpdated
        ItemCollection<QueryOutcome> persistedEntryItems = null;
        try{
            persistedEntryItems = index.query(spec);
        }catch(Exception e){
            LOG.error("Exception " + e + e.getMessage());   
        }
        
        Iterator<Item> itemsIterator = persistedEntryItems.iterator();
        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next();
            feedPage.add(item.toJSONPretty());
        }
        return feedPage;
    }

    /**
     * This  is a overloaded method is used to build a query based on the condition expression(for ex with where clause)and other params
     *
     * @param dynamoDB:            object of dynamoDb
     * @param conditionExpression: Filtered Exppression based on which results are filtered(where clause conditions)
     * @param valueMap:            map for keeping the mapped  values passed in a condition expression
     * @return List of String in json format.
     */
    public List<String> getQueryBuilderMethod(DynamoDB dynamoDB, String conditionExpression,String filterExpression, ValueMap valueMap) {

        List<String> feedPage = new ArrayList<>();
        Table table = dynamoDB.getTable("entries");
        Index index = table.getIndex("global-feed-index");
        QuerySpec spec;
        if(null != filterExpression){
          spec  = new QuerySpec()
            .withKeyConditionExpression(conditionExpression)
            .withFilterExpression(filterExpression)
            .withScanIndexForward(true)
            .withValueMap(valueMap);
        }else{
            spec  = new QuerySpec()
            .withKeyConditionExpression(conditionExpression)
            .withScanIndexForward(true)
            .withValueMap(valueMap);
        }
         
        ItemCollection<QueryOutcome> persistedEntryItems = null;
        try{
            persistedEntryItems   = index.query(spec);
        }catch(Exception e){
            LOG.error("Exception : " + e + e.getMessage()) ;
        }
        
        Iterator<Item> itemsIterator = persistedEntryItems.iterator();
        while (itemsIterator.hasNext()) {
            Item item = itemsIterator.next();
            feedPage.add(item.toJSONPretty());
        }
        return feedPage;
    }
}
