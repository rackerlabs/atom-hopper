package org.atomhopper.jdbc.adapter;

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
import org.atomhopper.jdbc.model.PersistedEntry;
import org.atomhopper.jdbc.query.SearchToSqlConverter;
import org.atomhopper.jdbc.query.SearchType;
import org.atomhopper.jdbc.query.SqlBuilder;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;

/**
 * Implements the FeedSource interface for retrieving feed entries from a datastore.  This class implements
 * the following:
 *
 * <ul>
 *     <li>Enabling an archive feed</li>
 *     <li>Records performance metrics</li>
 *     <li>Generating the feed entries from PersistedEntry instances</li>
 *     <li>Accessing data from a postgres table where all categories are treated equally</li>
 *     <li>Read categories with predefined prefixes from specified columns for better search performance</li>
 * </ul>
 *
 * Mapping category prefixes to postgres columns is done through the following:
 * <ul>
 *     <li>PrefixColumnMap - maps a prefix key to a column name.  E.g., 'tid' to 'tenantid'</li>
 *     <li>Delimiter - used to extract the prefix from a category.  E.g., if the delimiter is ':' the category
 *     value would be 'tid:1234'</li>
 * </ul>
 *
 */
public class JdbcFeedSource implements FeedSource, InitializingBean {

    static Logger LOG = LoggerFactory.getLogger(
          JdbcFeedSource.class );

    private static final String MARKER_EQ = "?marker=";
    private static final String LIMIT_EQ = "?limit=";
    private static final String AND_SEARCH_EQ = "&search=";
    private static final String AND_LIMIT_EQ = "&limit=";
    private static final String AND_MARKER_EQ = "&marker=";
    private static final String AND_DIRECTION_EQ = "&direction=";
    private static final String AND_DIRECTION_EQ_BACKWARD = "&direction=backward";
    private static final String AND_DIRECTION_EQ_FORWARD = "&direction=forward";
    private static final String MOCK_LAST_MARKER = "last";
    private static final String UUID_URI_SCHEME = "urn:uuid";

    private static final int PAGE_SIZE = 25;
    private JdbcTemplate jdbcTemplate;
    private boolean enableTimers = false;
    private boolean enableLoggingOnShortPage = false;
    private int feedHeadDelayInSeconds = 2;

    private Map<String, String> mapPrefix = new HashMap<String, String>();
    private Map<String, String> mapColumn = new HashMap<String, String>();

    private String split;

    private AdapterHelper helper = new AdapterHelper();

    private SearchToSqlConverter getSearchToSqlConverter() {

        return new SearchToSqlConverter( mapPrefix, split );
    }

    private RowMapper getRowMapper() {

        return new EntryRowMapper();
    }

    protected JdbcTemplate getJdbcTemplate() {

        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setArchiveUrl( URL url ) {

        helper.setArchiveUrl( url );
    }


    @Override
    public void setCurrentUrl( URL urlCurrent ) {

        helper.setCurrentUrl( urlCurrent );
    }

    public void setEnableTimers(Boolean enableTimers) {
        this.enableTimers = enableTimers;
    }

    public void setEnableLoggingOnShortPage(Boolean enableLoggingOnShortPage) {
        this.enableLoggingOnShortPage = enableLoggingOnShortPage;
    }

    public Boolean getEnableLoggingOnShortPage() {
        return enableLoggingOnShortPage;
    }

    static void setLog(Logger log) {
        LOG = log;
    }

    public void setFeedHeadDelayInSeconds(int feedHeadDelayInSeconds) {
        this.feedHeadDelayInSeconds = feedHeadDelayInSeconds;
    }

    public void setPrefixColumnMap( Map<String, String> prefix ) {

        mapPrefix = new HashMap<String, String>( prefix );

        mapColumn = new HashMap<String, String>();

        for( String key : mapPrefix.keySet() ) {

            mapColumn.put( mapPrefix.get( key ), key );
        }
    }

    public void setDelimiter( String splitParam ) {

        split = splitParam;
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterPropertiesSet() {

        if( split != null ^ !mapPrefix.isEmpty() ) {

            throw new IllegalArgumentException( "The 'delimiter' and 'prefixColumnMap' field must both be defined" );
        }
    }


    private void addFeedSelfLink(Feed feed, final String baseFeedUri,
                                 final GetFeedRequest getFeedRequest,
                                 final int pageSize, final String searchString) {

        StringBuilder queryParams = new StringBuilder();
        boolean markerIsSet = false;

        queryParams.append(baseFeedUri).append(LIMIT_EQ).append(
              String.valueOf(pageSize));

        if (searchString.length() > 0) {
            queryParams.append(AND_SEARCH_EQ).append(urlEncode(searchString));
        }
        if (getFeedRequest.getPageMarker() != null && getFeedRequest.getPageMarker().length() > 0) {
            queryParams.append(AND_MARKER_EQ).append(getFeedRequest.getPageMarker());
            markerIsSet = true;
        }
        if (markerIsSet) {
            queryParams.append(AND_DIRECTION_EQ).append(getFeedRequest.getDirection());
        } else {
            queryParams.append(AND_DIRECTION_EQ_BACKWARD);
            if (queryParams.toString().equalsIgnoreCase(
                  baseFeedUri + LIMIT_EQ + "25" + AND_DIRECTION_EQ_BACKWARD)) {
                // They are calling the feedhead, just use the base feed uri
                // This keeps the validator at http://validator.w3.org/ happy
                queryParams.delete(0, queryParams.toString().length()).append(
                      baseFeedUri);
            }
        }
        feed.addLink(queryParams.toString()).setRel( Link.REL_SELF );
    }

    protected List<PersistedEntry> getFeedBackward( String feedName,
                                                    Date markerTimestamp,
                                                    long markerId,
                                                    String searchString,
                                                    int pageSize ) {

        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString( searchString );


        List<PersistedEntry> feedPage;
        Object[] parmsBack = createParams( feedName, markerTimestamp, markerId, pageSize, categoriesList );
        SqlBuilder sqlBac = new SqlBuilder( getSearchToSqlConverter() ).searchString(searchString);
        sqlBac.searchType( SearchType.FEED_BACKWARD);

        feedPage = getJdbcTemplate().query(sqlBac.toString(),parmsBack, getRowMapper() );

        return feedPage;
    }

    protected List<PersistedEntry> getFeedForward( String feedName,
                                                   Date markerTimestamp,
                                                   long markerId,
                                                   String searchString,
                                                   int pageSize,
                                                   int feedHeadDelayInSeconds) {

        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString( searchString );

        List<PersistedEntry> feedPage;
        Object[] parmsFor = createParams( feedName, markerTimestamp, markerId, pageSize, categoriesList );
        SqlBuilder sqlFor = new SqlBuilder( getSearchToSqlConverter() ).searchString(searchString);
        sqlFor.searchType( SearchType.FEED_FORWARD).feedHeadDelayInSeconds( feedHeadDelayInSeconds );
        feedPage = getJdbcTemplate().query( sqlFor.toString(), parmsFor, getRowMapper() );

        return feedPage;
    }

    protected PersistedEntry getEntry(final String entryId, final String feedName) {
        final String entrySQL = "SELECT * FROM entries WHERE feed = ? AND entryid = ?";
        List<PersistedEntry> entry = getJdbcTemplate()
              .query(entrySQL, new Object[]{feedName, entryId}, getRowMapper());
        return entry.size() > 0 ? entry.get(0) : null;
    }

    protected PersistedEntry getEntryByTimestamp(final DateTime markerDate, final String feedName, PageDirection direction) {

        SqlBuilder sqlBuilder = new SqlBuilder( getSearchToSqlConverter() )
              .searchType(direction == PageDirection.BACKWARD ? SearchType.BY_TIMESTAMP_BACKWARD : SearchType.BY_TIMESTAMP_FORWARD)
              .startingTimestamp(markerDate);

        List<PersistedEntry> entry = getJdbcTemplate()
              .query(sqlBuilder.toString(), new Object[]{feedName}, getRowMapper());
        return entry.size() > 0 ? entry.get(0) : null;
    }

    private void addFeedCurrentLink(Feed hydratedFeed, final String baseFeedUri) {

        String url = helper.isArchived() ? helper.getCurrentUrl() : baseFeedUri;

        hydratedFeed.addLink( url, Link.REL_CURRENT);
    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries,
                             GetFeedRequest getFeedRequest, final int pageSize) {

        final Feed hydratedFeed = abdera.newFeed();
        final String baseFeedUri = decode(getFeedRequest.urlFor(
              new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));
        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        if ( helper.isArchived() ) {

            helper.addArchiveNode( hydratedFeed );
        }

        // Set the feed links
        addFeedCurrentLink(hydratedFeed, baseFeedUri);
        addFeedSelfLink( hydratedFeed, baseFeedUri, getFeedRequest, pageSize, searchString );


        PersistedEntry nextEntry = null;

        // TODO: We should have a link builder method for these
        if (!(persistedEntries.isEmpty())) {
            hydratedFeed.setId(UUID_URI_SCHEME + UUID.randomUUID().toString());
            hydratedFeed.setTitle(persistedEntries.get(0).getFeed());

            // Set the previous link
            hydratedFeed.addLink(new StringBuilder()
                                       .append(baseFeedUri).append(MARKER_EQ)
                                       .append(persistedEntries.get(0).getEntryId())
                                       .append(AND_LIMIT_EQ).append(String.valueOf(pageSize))
                                       .append(AND_SEARCH_EQ).append(urlEncode(searchString))
                                       .append(AND_DIRECTION_EQ_FORWARD).toString())
                  .setRel( helper.getPrevLink() );

            final PersistedEntry lastEntryInCollection = persistedEntries.get(persistedEntries.size() - 1);

            nextEntry = getNextMarker(lastEntryInCollection, getFeedRequest.getFeedName(), searchString);

            if (nextEntry != null) {
                // Set the next link
                hydratedFeed.addLink(new StringBuilder().append(baseFeedUri)
                                           .append(MARKER_EQ).append(nextEntry.getEntryId())
                                           .append(AND_LIMIT_EQ).append(String.valueOf(pageSize))
                                           .append(AND_SEARCH_EQ).append(urlEncode(searchString))
                                           .append(AND_DIRECTION_EQ_BACKWARD).toString())
                      .setRel( helper.getNextLink() );
            }
        }

        if ( nextEntry == null && helper.getArchiveUrl() != null ) {
            hydratedFeed.addLink(new StringBuilder().append( helper.getArchiveUrl() ).append( LIMIT_EQ).append(String.valueOf(pageSize))
                                       .append(AND_DIRECTION_EQ_BACKWARD).toString())
                  .setRel( FeedSource.REL_ARCHIVE_NEXT );
        }

        for (PersistedEntry persistedFeedEntry : persistedEntries) {
            hydratedFeed.addEntry(hydrateEntry(persistedFeedEntry, abdera));
        }

        if ( getEnableLoggingOnShortPage() ) {
            if ( hydratedFeed.getEntries() != null && hydratedFeed.getEntries().size() < pageSize ) {
                LOG.warn("User requested " + getFeedRequest.getFeedName() + " feed with limit " + pageSize + ", but returning only " + hydratedFeed.getEntries().size());
                List<Entry> entries = hydratedFeed.getEntries();
                StringBuilder sb = new StringBuilder();
                for (int idx=0; idx<entries.size(); idx++) {
                    Entry entry = entries.get(idx);
                    sb.append(entry.getId() + ", ");
                }
                LOG.warn("UUIDs: " + sb.toString());
            } else if ( hydratedFeed.getEntries() == null ) {
                LOG.warn("User requested " + getFeedRequest.getFeedName() + " feed with limit " + pageSize + ", but no entries are available");
            }
        }

        return hydratedFeed;
    }

    private Entry hydrateEntry(PersistedEntry persistedEntry, Abdera abderaReference) {

        final Document<Entry> hydratedEntryDocument = abderaReference.getParser().parse(
              new StringReader( persistedEntry.getEntryBody() ) );

        Entry entry = null;

        if (hydratedEntryDocument != null) {
            entry = hydratedEntryDocument.getRoot();
            entry.setUpdated(persistedEntry.getDateLastUpdated());
            entry.setPublished(persistedEntry.getCreationDate());
        }

        return entry;
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {

        final PersistedEntry entry = getEntry(getEntryRequest.getEntryId(), getEntryRequest.getFeedName());

        AdapterResponse<Entry> response = ResponseBuilder.notFound();

        if (entry != null) {
            response = ResponseBuilder.found(hydrateEntry(entry, getEntryRequest.getAbdera()));
        }

        return response;
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        AdapterResponse<Feed> response;

        TimerContext context = null;

        int pageSize = PAGE_SIZE;
        final String pageSizeString = getFeedRequest.getPageSize();
        if ( StringUtils.isNotBlank( pageSizeString )) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        final String marker = getFeedRequest.getPageMarker();
        final String startingAt = getFeedRequest.getStartingAt();
        if ( StringUtils.isNotBlank(marker) && StringUtils.isNotBlank(startingAt) ) {
            response = ResponseBuilder.badRequest("'marker' parameter can not be used together with the 'startingAt' parameter");
            return response;
        }

        try {

            if ( StringUtils.isBlank(marker) && StringUtils.isBlank(startingAt) ) {
                context = startTimer(String.format("get-feed-head-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedHead(getFeedRequest, pageSize);
            } else if ( StringUtils.isNotBlank(marker) && marker.equals(MOCK_LAST_MARKER)) {
                context = startTimer(String.format("get-last-page-%s", getMetricBucketForPageSize(pageSize)));
                response = getLastPage(getFeedRequest, pageSize);
            } else if ( StringUtils.isNotBlank(marker) ) {
                context = startTimer(String.format("get-feed-page-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedPage(getFeedRequest, marker, pageSize);
            } else {
                // we process 'startingAt' parameter here
                context = startTimer(String.format("get-feed-page-startingAt-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedPageByTimestamp(getFeedRequest, startingAt, pageSize);
            }
        } catch (IllegalArgumentException iae) {
            response = ResponseBuilder.badRequest(iae.getMessage());
        } finally {
            stopTimer(context);
        }

        return response;
    }

    private AdapterResponse<Feed> getFeedHead(GetFeedRequest getFeedRequest,
                                              int pageSize) {
        final Abdera abdera = getFeedRequest.getAbdera();

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        List<PersistedEntry> persistedEntries = getFeedHead(getFeedRequest.getFeedName(), pageSize, searchString);

        Feed hydratedFeed = hydrateFeed(abdera, persistedEntries, getFeedRequest, pageSize);

        // Set the last link in the feed head
        final String baseFeedUri = decode(getFeedRequest.urlFor(
              new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));

        if( !helper.isArchived() ) {

            hydratedFeed.addLink(
                  new StringBuilder().append(baseFeedUri)
                        .append(MARKER_EQ).append(MOCK_LAST_MARKER)
                        .append(AND_LIMIT_EQ).append(String.valueOf(pageSize))
                        .append(AND_SEARCH_EQ).append(urlEncode(searchString))
                        .append(AND_DIRECTION_EQ_BACKWARD).toString())
                  .setRel(Link.REL_LAST);
        }

        return ResponseBuilder.found(hydratedFeed);
    }

    private AdapterResponse<Feed> getFeedPage(GetFeedRequest getFeedRequest, String marker, int pageSize) {

        final String pageDirectionValue = getFeedRequest.getDirection();
        PageDirection pageDirection = PageDirection.FORWARD;
        if ( StringUtils.isNotEmpty(pageDirectionValue) ) {
            pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());
        }

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        PersistedEntry entryMarker = getEntry( marker, getFeedRequest.getFeedName() );
        if ( entryMarker == null ) {
            return ResponseBuilder.notFound("No entry with specified marker found");
        }

        final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                                      enhancedGetFeedPage(getFeedRequest.getFeedName(),
                                                          entryMarker.getDateLastUpdated(),
                                                          entryMarker.getId(),
                                                          pageDirection,
                                                          searchString, pageSize),
                                      getFeedRequest, pageSize);
        return ResponseBuilder.found(feed);
    }

    private AdapterResponse<Feed> getFeedPageByTimestamp(GetFeedRequest getFeedRequest, String startingAt, int pageSize) {

        final String pageDirectionValue = getFeedRequest.getDirection();
        PageDirection pageDirection = PageDirection.FORWARD;
        if ( StringUtils.isNotEmpty(pageDirectionValue) ) {
            pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());
        }

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        DateTimeFormatter isoDTF = ISODateTimeFormat.dateTime();
        DateTime startAt = isoDTF.parseDateTime(startingAt);
        PersistedEntry entryMarker = getEntryByTimestamp(startAt, getFeedRequest.getFeedName(), pageDirection);
        if ( entryMarker == null ) {
            return ResponseBuilder.notFound("No entry with specified startingAt timestamp found");
        }

        final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                                      enhancedGetFeedPage(getFeedRequest.getFeedName(),
                                                          entryMarker.getDateLastUpdated(),
                                                          entryMarker.getId(),
                                                          pageDirection,
                                                          searchString, pageSize),
                                      getFeedRequest, pageSize);
        return ResponseBuilder.found(feed);
    }

    private AdapterResponse<Feed> getLastPage(GetFeedRequest getFeedRequest, int pageSize) {

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
        AdapterResponse<Feed> response;

        final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                                      enhancedGetLastPage(getFeedRequest.getFeedName(), pageSize, searchString),
                                      getFeedRequest, pageSize);
        response = ResponseBuilder.found(feed);

        return response;
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<PersistedEntry> enhancedGetFeedPage(final String feedName, final Date markerTimestamp,
                                                     final long markerId,
                                                     final PageDirection direction, final String searchString,
                                                     final int pageSize) {

        List<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();

        TimerContext context = null;

        boolean hasCats = !searchString.trim().isEmpty();

        try {
            switch (direction) {
                case FORWARD:


                    if (  hasCats ) {
                        context = startTimer(String.format("db-get-feed-page-forward-with-cats-%s",
                                                           getMetricBucketForPageSize(pageSize)));
                    } else {
                        context = startTimer(
                              String.format("db-get-feed-page-forward-%s", getMetricBucketForPageSize(pageSize)));
                    }
                    feedPage = getFeedForward( feedName,
                                               markerTimestamp,
                                               markerId,
                                               searchString,
                                               pageSize,
                                               feedHeadDelayInSeconds );

                    Collections.reverse(feedPage);
                    break;

                case BACKWARD:


                    if (  hasCats ) {
                        context = startTimer(String.format("db-get-feed-page-backward-with-cats-%s",
                                                           getMetricBucketForPageSize(pageSize)));
                    } else {
                        context = startTimer(
                              String.format("db-get-feed-page-backward-%s", getMetricBucketForPageSize(pageSize)));
                    }
                    feedPage = getFeedBackward( feedName,
                                                markerTimestamp,
                                                markerId,
                                                searchString,
                                                pageSize );


                    break;
            }
        } finally {
            stopTimer(context);
        }

        return feedPage;
    }


    private List<PersistedEntry> getFeedHead(final String feedName, final int pageSize, final String searchString) {

        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString(searchString);
        int numCats = categoriesList.size();

        Object[] parms = null;

        if (numCats > 0) {
            parms = new Object[numCats + 2];
            int index = 0;
            parms[index++] = feedName;
            for (String s : categoriesList) {
                parms[index++] = s;
            }
            parms[index++] = pageSize;
        } else {
            parms = new Object[]{feedName, pageSize};
        }

        TimerContext context = null;
        try {
            if (numCats > 0) {
                context = startTimer(
                      String.format("db-get-feed-head-with-cats-%s", getMetricBucketForPageSize(pageSize)));
            } else {
                context = startTimer(String.format("db-get-feed-head-%s", getMetricBucketForPageSize(pageSize)));
            }

            SqlBuilder sql = new SqlBuilder( getSearchToSqlConverter() ).searchType(SearchType.FEED_HEAD).searchString(searchString)
                  .feedHeadDelayInSeconds(feedHeadDelayInSeconds);

            return jdbcTemplate.query(sql.toString(), parms, getRowMapper() );

        } finally {
            stopTimer(context);
        }
    }

    private List<PersistedEntry> enhancedGetLastPage(final String feedName, final int pageSize,
                                                     final String searchString) {

        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString(searchString);
        int numCats = categoriesList.size();

        Object[] parms = null;

        if (numCats > 0) {
            parms = new Object[numCats + 2];
            int index = 0;
            parms[index++] = feedName;
            for (String s : categoriesList) {
                parms[index++] = s;
            }
            parms[index++] = pageSize;

        } else {
            parms = new Object[]{feedName, pageSize};
        }

        TimerContext context = null;
        List<PersistedEntry> lastPersistedEntries;
        try {
            if (numCats > 0) {
                context = startTimer(
                      String.format("db-get-last-page-with-cats-%s", getMetricBucketForPageSize(pageSize)));
            } else {
                context = startTimer(String.format("db-get-last-page-%s", getMetricBucketForPageSize(pageSize)));
            }

            SqlBuilder sql = new SqlBuilder( getSearchToSqlConverter() ).searchType( SearchType.LAST_PAGE ).searchString( searchString )
                  .feedHeadDelayInSeconds( feedHeadDelayInSeconds );

            lastPersistedEntries = jdbcTemplate.query(sql.toString(), parms, getRowMapper());

        } finally {
            stopTimer(context);
        }

        Collections.reverse(lastPersistedEntries);

        return lastPersistedEntries;

    }

    private PersistedEntry getNextMarker(final PersistedEntry persistedEntry, final String feedName,
                                         final String searchString) {

        List<String> categoriesList = getSearchToSqlConverter().getParamsFromSearchString(searchString);
        int numCats = categoriesList.size();

        Object[] parms = null;

        if (categoriesList.size() > 0) {
            parms = new Object[numCats * 2 + 5];
            int index = 0;
            parms[index++] = feedName;
            parms[index++] = persistedEntry.getDateLastUpdated();
            parms[index++] = persistedEntry.getId();
            for (String s : categoriesList) {
                parms[index++] = s;
            }
            parms[index++] = feedName;
            parms[index++] = persistedEntry.getDateLastUpdated();
            for (String s : categoriesList) {
                parms[index++] = s;
            }

        } else {
            parms = new Object[]{feedName, persistedEntry.getDateLastUpdated(), persistedEntry.getId(),
                  feedName, persistedEntry.getDateLastUpdated()};
        }

        SqlBuilder sql = new SqlBuilder( getSearchToSqlConverter() ).searchType(SearchType.NEXT_LINK).searchString(searchString);

        List<PersistedEntry> nextEntry = jdbcTemplate
              .query(sql.toString(), parms, getRowMapper());

        return nextEntry.size() > 0 ? nextEntry.get(0) : null;
    }

    private String urlEncode(String searchString) {
        try {
            return URLEncoder.encode( searchString, "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            //noop - should never get here
            return "";
        }
    }

    private TimerContext startTimer(String name) {
        if (enableTimers) {
            final com.yammer.metrics.core.Timer timer = Metrics.newTimer( getClass(), name, TimeUnit.MILLISECONDS,
                                                                          TimeUnit.SECONDS );
            TimerContext context = timer.time();
            return context;
        } else {
            return null;
        }
    }

    private void stopTimer(TimerContext context) {
        if (enableTimers && context != null) {
            context.stop();
        }
    }

    private String getMetricBucketForPageSize(final int pageSize) {
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

    protected Object[] createParams( String feedName,
                                     Date markerTimestamp,
                                     long markerId,
                                     int pageSize,
                                     List<String> categoriesList ) {

        int numCats = categoriesList.size();

        Object[] parms = null;

        if (numCats > 0) {
            parms = new Object[numCats * 2 + 7];
            int index = 0;
            parms[index++] = feedName;
            parms[index++] = markerTimestamp;
            parms[index++] = markerId;
            for (String s : categoriesList) {
                parms[index++] = s;
            }
            parms[index++] = feedName;
            parms[index++] = markerTimestamp;
            for (String s : categoriesList) {
                parms[index++] = s;
            }
            parms[index++] = pageSize;
            parms[index++] = pageSize;
        } else {
            parms = new Object[]{feedName, markerTimestamp, markerId,
                  feedName, markerTimestamp, pageSize, pageSize};
        }
        return parms;
    }

    public class EntryRowMapper implements RowMapper {

        @Override
        public Object mapRow( ResultSet rs, int rowNum ) throws SQLException {
            EntryResultSetExtractor extractor = new EntryResultSetExtractor();
            return extractor.extractData(rs);
        }
    }

    public class EntryResultSetExtractor implements ResultSetExtractor {


        @Override
        public Object extractData( ResultSet rs ) throws SQLException, DataAccessException {

            PersistedEntry entry = new PersistedEntry();
            entry.setId(rs.getLong("id"));
            entry.setFeed(rs.getString("feed"));
            entry.setCreationDate(rs.getTimestamp("creationdate"));
            entry.setDateLastUpdated(rs.getTimestamp("datelastupdated"));
            entry.setEntryBody(rs.getString("entrybody"));
            entry.setEntryId(rs.getString("entryid"));


            List<String> cats = new ArrayList<String>( Arrays.asList( (String[])rs.getArray( "categories" ).getArray() ) );

            for( String column : mapColumn.keySet() ) {

                if( rs.getString( column) != null ) {

                    cats.add( mapColumn.get( column) + split + rs.getString( column) );
                }
            }

            entry.setCategories( cats.toArray( new String[ 0 ]) );
            return entry;
        }
    }
}
