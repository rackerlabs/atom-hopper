package org.atomhopper.jdbc.adapter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.TimerContext;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.jdbc.model.PersistedEntry;
import org.atomhopper.jdbc.query.*;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;


public class JdbcFeedSource implements FeedSource {

    private static final Logger LOG = LoggerFactory.getLogger(
            JdbcFeedSource.class);

    private static final String MARKER_EQ = "?marker=";
    private static final String LIMIT_EQ = "?limit=";
    private static final String AND_SEARCH_EQ = "&search=";
    private static final String AND_LIMIT_EQ = "&limit=";
    private static final String AND_MARKER_EQ = "&marker=";
    private static final String AND_DIRECTION_EQ = "&direction=";
    private static final String AND_DIRECTION_EQ_BACKWARD = "&direction=backward";
    private static final String AND_DIRECTION_EQ_FORWARD = "&direction=forward";
    private static final String MOCK_LAST_MARKER = "last";

    private static final int PAGE_SIZE = 25;
    private JdbcTemplate jdbcTemplate;
    private boolean enableTimers = false;
    private int feedHeadDelayInSeconds = 2;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setEnableTimers(Boolean enableTimers) {
        this.enableTimers = enableTimers;
    }

    public void setFeedHeadDelayInSeconds(int feedHeadDelayInSeconds) {
        this.feedHeadDelayInSeconds = feedHeadDelayInSeconds;
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");
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
        feed.addLink(queryParams.toString()).setRel(Link.REL_SELF);
    }

    private void addFeedCurrentLink(Feed hydratedFeed, final String baseFeedUri) {

        hydratedFeed.addLink(baseFeedUri, Link.REL_CURRENT);
    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries,
                             GetFeedRequest getFeedRequest, final int pageSize) {

        final Feed hydratedFeed = abdera.newFeed();
        final String uuidUriScheme = "urn:uuid:";
        final String baseFeedUri = decode(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));
        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        // Set the feed links
        addFeedCurrentLink(hydratedFeed, baseFeedUri);
        addFeedSelfLink(hydratedFeed, baseFeedUri, getFeedRequest, pageSize, searchString);

        // TODO: We should have a link builder method for these
        if (!(persistedEntries.isEmpty())) {
            hydratedFeed.setId(uuidUriScheme + UUID.randomUUID().toString());
            hydratedFeed.setTitle(persistedEntries.get(0).getFeed());

            // Set the previous link
            hydratedFeed.addLink(new StringBuilder()
                                         .append(baseFeedUri).append(MARKER_EQ)
                                         .append(persistedEntries.get(0).getEntryId())
                                         .append(AND_LIMIT_EQ).append(String.valueOf(pageSize))
                                         .append(AND_SEARCH_EQ).append(urlEncode(searchString))
                                         .append(AND_DIRECTION_EQ_FORWARD).toString())
                    .setRel(Link.REL_PREVIOUS);

            final PersistedEntry lastEntryInCollection = persistedEntries.get(persistedEntries.size() - 1);

            PersistedEntry nextEntry = getNextMarker(lastEntryInCollection, getFeedRequest.getFeedName(), searchString);

            if (nextEntry != null) {
                // Set the next link
                hydratedFeed.addLink(new StringBuilder().append(baseFeedUri)
                                             .append(MARKER_EQ).append(nextEntry.getEntryId())
                                             .append(AND_LIMIT_EQ).append(String.valueOf(pageSize))
                                             .append(AND_SEARCH_EQ).append(urlEncode(searchString))
                                             .append(AND_DIRECTION_EQ_BACKWARD).toString())
                        .setRel(Link.REL_NEXT);
            }
        }

        for (PersistedEntry persistedFeedEntry : persistedEntries) {
            hydratedFeed.addEntry(hydrateEntry(persistedFeedEntry, abdera));
        }

        return hydratedFeed;
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

        if (StringUtils.isNotBlank(pageSizeString)) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        final String marker = getFeedRequest.getPageMarker();

        try {
            if ((StringUtils.isBlank(marker))) {
                context = startTimer(String.format("get-feed-head-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedHead(getFeedRequest, pageSize, feedHeadDelayInSeconds);
            } else if (marker.equals(MOCK_LAST_MARKER)) {
                context = startTimer(String.format("get-last-page-%s", getMetricBucketForPageSize(pageSize)));
                response = getLastPage(getFeedRequest, pageSize);
            } else {
                context = startTimer(String.format("get-feed-page-%s", getMetricBucketForPageSize(pageSize)));
                response = getFeedPage(getFeedRequest, marker, pageSize);
            }
        } catch (IllegalArgumentException iae) {
            response = ResponseBuilder.badRequest(iae.getMessage());
        } finally {
            stopTimer(context);
        }

        return response;
    }

    private AdapterResponse<Feed> getFeedHead(GetFeedRequest getFeedRequest,
                                              int pageSize, int feedHeadDelayInSeconds) {
        final Abdera abdera = getFeedRequest.getAbdera();

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        List<PersistedEntry> persistedEntries = getFeedHead(getFeedRequest.getFeedName(), pageSize, searchString);

        Feed hydratedFeed = hydrateFeed(abdera, persistedEntries, getFeedRequest, pageSize);

        // Set the last link in the feed head
        final String baseFeedUri = decode(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));

        hydratedFeed.addLink(
                new StringBuilder().append(baseFeedUri)
                        .append(MARKER_EQ).append(MOCK_LAST_MARKER)
                        .append(AND_LIMIT_EQ).append(String.valueOf(pageSize))
                        .append(AND_SEARCH_EQ).append(urlEncode(searchString))
                        .append(AND_DIRECTION_EQ_BACKWARD).toString())
                .setRel(Link.REL_LAST);

        return ResponseBuilder.found(hydratedFeed);
    }

    private AdapterResponse<Feed> getFeedPage(GetFeedRequest getFeedRequest, String marker, int pageSize) {

        AdapterResponse<Feed> response;
        PageDirection pageDirection;

        final String pageDirectionValue = getFeedRequest.getDirection();
        pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());

        final PersistedEntry markerEntry = getEntry(marker, getFeedRequest.getFeedName());

        if (markerEntry != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                                          enhancedGetFeedPage(getFeedRequest.getFeedName(),
                                                              markerEntry, pageDirection,
                                                              searchString, pageSize),
                                          getFeedRequest, pageSize);
            response = ResponseBuilder.found(feed);
        } else {
            response = ResponseBuilder.notFound(
                    "No entry with specified marker found");
        }

        return response;
    }

    private AdapterResponse<Feed> getLastPage(GetFeedRequest getFeedRequest, int pageSize) {

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
        AdapterResponse<Feed> response;

        int totalFeedEntryCount = getFeedCount(getFeedRequest.getFeedName(), searchString);

        int lastPageSize = totalFeedEntryCount % pageSize;
        if (lastPageSize == 0) {
            lastPageSize = pageSize;
        }

        final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                                      enhancedGetLastPage(getFeedRequest.getFeedName(), lastPageSize, searchString),
                                      getFeedRequest, pageSize);
        response = ResponseBuilder.found(feed);

        return response;
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<PersistedEntry> enhancedGetFeedPage(final String feedName, final PersistedEntry markerEntry,
                                                     final PageDirection direction, final String searchString,
                                                     final int pageSize) {

        List<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();

        TimerContext context = null;

        SqlBuilder sql = new SqlBuilder().searchString(searchString);
        List<String> categoriesList = SearchToSqlConverter.getParamsFromSearchString(searchString);

        int numCats = categoriesList.size();

        Object[] parms = null;

        if (numCats > 0) {
            parms = new Object[numCats * 2 + 7];
            int index = 0;
            parms[index++] = feedName;
            parms[index++] = markerEntry.getDateLastUpdated();
            parms[index++] = markerEntry.getId();
            for (String s : categoriesList) {
                parms[index++] = s;
            }
            parms[index++] = feedName;
            parms[index++] = markerEntry.getDateLastUpdated();
            for (String s : categoriesList) {
                parms[index++] = s;
            }
            parms[index++] = pageSize;
            parms[index++] = pageSize;
        } else {
            parms = new Object[]{feedName, markerEntry.getDateLastUpdated(), markerEntry.getId(),
                    feedName, markerEntry.getDateLastUpdated(), pageSize, pageSize};
        }

        try {
            switch (direction) {
                case FORWARD:

                    sql.searchType(SearchType.FEED_FORWARD);

                    if (numCats > 0) {
                        context = startTimer(String.format("db-get-feed-page-forward-with-cats-%s",
                                                           getMetricBucketForPageSize(pageSize)));
                    } else {
                        context = startTimer(
                                String.format("db-get-feed-page-forward-%s", getMetricBucketForPageSize(pageSize)));
                    }

                    feedPage = jdbcTemplate.query(sql.toString(),parms,new EntryRowMapper());

                    Collections.reverse(feedPage);
                    break;

                case BACKWARD:

                    sql.searchType(SearchType.FEED_BACKWARD);

                    if (numCats > 0) {
                        context = startTimer(String.format("db-get-feed-page-backward-with-cats-%s",
                                                           getMetricBucketForPageSize(pageSize)));
                    } else {
                        context = startTimer(
                                String.format("db-get-feed-page-backward-%s", getMetricBucketForPageSize(pageSize)));
                    }

                    feedPage = jdbcTemplate.query(sql.toString(),parms,new EntryRowMapper());
                    break;
            }
        } finally {
            stopTimer(context);
        }

        return feedPage;
    }

    private PersistedEntry getEntry(final String entryId, final String feedName) {
        final String entrySQL = "SELECT * FROM entries WHERE feed = ? AND entryid = ?";
        List<PersistedEntry> entry = jdbcTemplate
                .query(entrySQL, new Object[]{feedName, entryId}, new EntryRowMapper());
        return entry.size() > 0 ? entry.get(0) : null;
    }

    private Integer getFeedCount(final String feedName, final String searchString) {

        SqlBuilder sql = new SqlBuilder().searchType(SearchType.FEED_COUNT).searchString(searchString);

        List<String> categoriesList = SearchToSqlConverter.getParamsFromSearchString(searchString);
        int numCats = categoriesList.size();

        Object[] parms = null;

        if (numCats > 0) {
            parms = new Object[numCats + 1];
            int index = 0;
            parms[index++] = feedName;
            for (String s : categoriesList) {
                parms[index++] = s;
            }
        } else {
            parms = new Object[]{feedName};
        }

        TimerContext context = null;
        try {
            if (numCats > 0) {
                context = startTimer("db-get-feed-count-with-cats");
            } else {
                context = startTimer("db-get-feed-count");
            }

            return jdbcTemplate.queryForInt(sql.toString(), parms);
        } finally {
            stopTimer(context);
        }
    }

    private List<PersistedEntry> getFeedHead(final String feedName, final int pageSize, final String searchString) {

        SqlBuilder sql = new SqlBuilder().searchType(SearchType.FEED_HEAD).searchString(searchString);

        List<String> categoriesList = SearchToSqlConverter.getParamsFromSearchString(searchString);
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
            return jdbcTemplate.query(sql.toString(), parms, new EntryRowMapper());
        } finally {
            stopTimer(context);
        }
    }

    private List<PersistedEntry> enhancedGetLastPage(final String feedName, final int pageSize,
                                                     final String searchString) {

        SqlBuilder sql = new SqlBuilder().searchType(SearchType.LAST_PAGE).searchString(searchString);

        List<String> categoriesList = SearchToSqlConverter.getParamsFromSearchString(searchString);
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
            lastPersistedEntries = jdbcTemplate.query(sql.toString(), parms, new EntryRowMapper());
        } finally {
            stopTimer(context);
        }

        Collections.reverse(lastPersistedEntries);

        return lastPersistedEntries;

    }

    private PersistedEntry getNextMarker(final PersistedEntry persistedEntry, final String feedName,
                                         final String searchString) {

        SqlBuilder sql = new SqlBuilder().searchType(SearchType.NEXT_LINK).searchString(searchString);

        List<String> categoriesList = SearchToSqlConverter.getParamsFromSearchString(searchString);
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

        List<PersistedEntry> nextEntry = jdbcTemplate
                    .query(sql.toString(), parms, new EntryRowMapper());

        return nextEntry.size() > 0 ? nextEntry.get(0) : null;
    }

    private String urlEncode(String searchString) {
        try {
            return URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            //noop - should never get here
            return "";
        }
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
}

