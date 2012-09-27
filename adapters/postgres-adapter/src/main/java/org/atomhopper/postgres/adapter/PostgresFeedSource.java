package org.atomhopper.postgres.adapter;

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
import org.atomhopper.postgres.model.PersistedEntry;
import org.atomhopper.postgres.query.CategoryStringGenerator;
import org.atomhopper.postgres.query.EntryRowMapper;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.StringReader;
import java.util.*;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;
import static org.apache.abdera.i18n.text.UrlEncoding.encode;


public class PostgresFeedSource implements FeedSource {

    private static final Logger LOG = LoggerFactory.getLogger(
            PostgresFeedSource.class);

    private static final int PAGE_SIZE = 25;
    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

        queryParams.append(baseFeedUri).append("?limit=").append(
                String.valueOf(pageSize));

        if (searchString.length() > 0) {
            queryParams.append("&search=").append(encode(searchString));
        }
        if (getFeedRequest.getPageMarker() != null && getFeedRequest.getPageMarker().length() > 0) {
            queryParams.append("&marker=").append(getFeedRequest.getPageMarker());
            markerIsSet = true;
        }
        if (markerIsSet) {
            queryParams.append("&direction=").append(getFeedRequest.getDirection());
        } else {
            queryParams.append("&direction=backward");
            if (queryParams.toString().equalsIgnoreCase(
                    baseFeedUri + "?limit=25&direction=backward")) {
                // They are calling the feedhead, just use the base feed uri
                // This keeps the validator at http://validator.w3.org/ happy
                queryParams.delete(0, queryParams.toString().length()).append(
                        baseFeedUri);
            }
        }
        feed.addLink(queryParams.toString()).setRel(Link.REL_SELF);
    }

    private void addFeedCurrentLink(Feed hyrdatedFeed, final String baseFeedUri) {

        hyrdatedFeed.addLink(baseFeedUri, Link.REL_CURRENT);
    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries,
                             GetFeedRequest getFeedRequest, final int pageSize) {

        final Feed hyrdatedFeed = abdera.newFeed();
        final String uuidUriScheme = "urn:uuid:";
        final String baseFeedUri = decode(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));
        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

        // Set the feed links
        addFeedCurrentLink(hyrdatedFeed, baseFeedUri);
        addFeedSelfLink(hyrdatedFeed, baseFeedUri, getFeedRequest, pageSize, searchString);

        // TODO: We should have a link builder method for these
        if (!(persistedEntries.isEmpty())) {
            hyrdatedFeed.setId(uuidUriScheme + UUID.randomUUID().toString());
            hyrdatedFeed.setTitle(persistedEntries.get(0).getFeed());

            // Set the previous link
            hyrdatedFeed.addLink(new StringBuilder()
                                         .append(baseFeedUri).append("?marker=")
                                         .append(persistedEntries.get(0).getEntryId())
                                         .append("&limit=").append(String.valueOf(pageSize))
                                         .append("&search=").append(encode(searchString))
                                         .append("&direction=forward").toString())
                    .setRel(Link.REL_PREVIOUS);

            final PersistedEntry lastEntryInCollection = persistedEntries.get(persistedEntries.size() - 1);
            final String nextLinkSQL = "SELECT * FROM entries where feed = ? and datelastupdated > ? ORDER BY datelastupdated LIMIT 1";
            final String nextLinkWithCatsSQL = "SELECT * FROM entries where feed = ? and datelastupdated > ? AND categories @> ?::varchar[] ORDER BY datelastupdated LIMIT 1";

            PersistedEntry nextEntry;
            if (searchString.length() > 0) {
                nextEntry = (PersistedEntry) jdbcTemplate
                        .queryForObject(nextLinkWithCatsSQL, new EntryRowMapper(), getFeedRequest.getFeedName(),
                                        lastEntryInCollection.getDateLastUpdated(),
                                        CategoryStringGenerator.getPostgresCategoryString(searchString));
            } else {
                nextEntry = (PersistedEntry) jdbcTemplate
                        .queryForObject(nextLinkSQL, new EntryRowMapper(), getFeedRequest.getFeedName(),
                                        lastEntryInCollection.getDateLastUpdated());
            }

            if (nextEntry != null) {
                // Set the next link
                hyrdatedFeed.addLink(new StringBuilder().append(baseFeedUri)
                                             .append("?marker=").append(nextEntry.getEntryId())
                                             .append("&limit=").append(String.valueOf(pageSize))
                                             .append("&search=").append(encode(searchString))
                                             .append("&direction=backward").toString())
                        .setRel(Link.REL_NEXT);
            }
        }

        for (PersistedEntry persistedFeedEntry : persistedEntries) {
            hyrdatedFeed.addEntry(hydrateEntry(persistedFeedEntry, abdera));
        }

        return hyrdatedFeed;
    }

    private Entry hydrateEntry(PersistedEntry persistedEntry, Abdera abderaReference) {

        final Document<Entry> hydratedEntryDocument = abderaReference.getParser().parse(
                new StringReader(persistedEntry.getEntryBody()));

        Entry entry = null;

        if (hydratedEntryDocument != null) {
            entry = hydratedEntryDocument.getRoot();
            entry.setUpdated(persistedEntry.getDateLastUpdated());
        }

        return entry;
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {

        final String getEntrySQL = "SELECT * FROM entries WHERE entryid = ?";

        final PersistedEntry entry = (PersistedEntry) jdbcTemplate.queryForObject(
                getEntrySQL, new EntryRowMapper(), getEntryRequest.getEntryId());

        AdapterResponse<Entry> response = ResponseBuilder.notFound();

        if (entry != null) {
            response = ResponseBuilder.found(hydrateEntry(entry, getEntryRequest.getAbdera()));
        }

        return response;
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        AdapterResponse<Feed> response;

        int pageSize = PAGE_SIZE;
        final String pageSizeString = getFeedRequest.getPageSize();

        if (StringUtils.isNotBlank(pageSizeString)) {
            pageSize = Integer.parseInt(pageSizeString);
        }

        final String marker = getFeedRequest.getPageMarker();

        if (StringUtils.isNotBlank(marker)) {
            response = getFeedPage(getFeedRequest, marker, pageSize);
        } else {
            response = getFeedHead(getFeedRequest, pageSize);
        }

        return response;
    }

    private AdapterResponse<Feed> getFeedHead(GetFeedRequest getFeedRequest,
                                              int pageSize) {
        final Abdera abdera = getFeedRequest.getAbdera();

        AdapterResponse<Feed> response = null;

        final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
        final String getFeedHeadSQL = "SELECT * FROM entries WHERE feed = ? LIMIT ?";
        final String getFeedHeadWithCatsSQL = "SELECT * FROM entries WHERE feed = ? AND categories @> ?::varchar[] LIMIT ?";

        List<PersistedEntry> persistedEntries;
        if (searchString.length() > 0) {
            persistedEntries = jdbcTemplate
                    .query(getFeedHeadWithCatsSQL, new Object[]{getFeedRequest.getFeedName(),
                            CategoryStringGenerator.getPostgresCategoryString(searchString), pageSize},
                           new EntryRowMapper());
        } else {
            persistedEntries = jdbcTemplate
                    .query(getFeedHeadSQL, new Object[]{getFeedRequest.getFeedName(), pageSize},
                           new EntryRowMapper());
        }

        Feed hyrdatedFeed = hydrateFeed(abdera, persistedEntries, getFeedRequest, pageSize);

        // Set the last link in the feed head
        final String baseFeedUri = decode(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));

        final String totalFeedEntryCountSQL = "SELECT COUNT(*) FROM entries WHERE feed = ?";
        final String totalFeedEntryCountWithCatsSQL = "SELECT COUNT(*) FROM entries WHERE feed = ? AND categories @> ?::varchar[]";

        int totalFeedEntryCount;

        if (searchString.length() > 0) {
            totalFeedEntryCount = jdbcTemplate
                    .queryForInt(totalFeedEntryCountWithCatsSQL, getFeedRequest.getFeedName(),
                                 CategoryStringGenerator.getPostgresCategoryString(searchString));
        } else {
            totalFeedEntryCount = jdbcTemplate
                    .queryForInt(totalFeedEntryCountSQL, getFeedRequest.getFeedName());
        }

        int lastPageSize = totalFeedEntryCount % pageSize;
        if (lastPageSize == 0) {
            lastPageSize = pageSize;
        }

        final String lastLinkQuerySQL = "SELECT * FROM entries WHERE feed = ? ORDER BY datelastupdated ASC LIMIT ?";
        final String lastLinkQueryWithCatsSQL = "SELECT * FROM entries WHERE feed = ? AND categories @> ?::varchar[] ORDER BY datelastupdated ASC LIMIT ?";

        List<PersistedEntry> lastPersistedEntries;
        if (searchString.length() > 0) {
            lastPersistedEntries = jdbcTemplate
                    .query(lastLinkQueryWithCatsSQL, new Object[]{getFeedRequest.getFeedName(),
                            CategoryStringGenerator.getPostgresCategoryString(searchString), lastPageSize},
                           new EntryRowMapper());
        } else {
            lastPersistedEntries = jdbcTemplate
                    .query(lastLinkQuerySQL, new Object[]{getFeedRequest.getFeedName(), lastPageSize},
                           new EntryRowMapper());
        }

        if (lastPersistedEntries != null && !(lastPersistedEntries.isEmpty())) {
            hyrdatedFeed.addLink(
                    new StringBuilder().append(baseFeedUri)
                            .append("?marker=").append(
                            lastPersistedEntries.get(lastPersistedEntries.size() - 1).getEntryId())
                            .append("&limit=").append(String.valueOf(pageSize))
                            .append("&search=").append(encode(searchString))
                            .append("&direction=backward").toString())
                    .setRel(Link.REL_LAST);
        }

        return ResponseBuilder.found(hyrdatedFeed);
    }

    private AdapterResponse<Feed> getFeedPage(GetFeedRequest getFeedRequest, String marker, int pageSize) {

        AdapterResponse<Feed> response;
        PageDirection pageDirection;

        try {
            final String pageDirectionValue = getFeedRequest.getDirection();
            pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());
        } catch (Exception iae) {
            return ResponseBuilder.badRequest(
                    "Marker must have a page direction specified as either \"forward\" or \"backward\"");
        }

        final String markerEntrySQL = "SELECT * FROM entries WHERE feed = ? AND entryid = ?";
        final PersistedEntry markerEntry = (PersistedEntry) jdbcTemplate
                .queryForObject(markerEntrySQL, new EntryRowMapper(),
                                getFeedRequest.getFeedName(), marker);

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

    private List<PersistedEntry> enhancedGetFeedPage(final String feedName, final PersistedEntry markerEntry,
                                                     final PageDirection direction, final String searchString,
                                                     final int pageSize) {

        List<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();

        switch (direction) {
            case FORWARD:

                final String forwardSQL = "SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? ORDER BY datelastupdated ASC LIMIT ?";
                final String forwardWithCatsSQL = "SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? AND categories @> ?::varchar[] ORDER BY datelastupdated ASC LIMIT ?";

                if (searchString.length() > 0) {
                    feedPage = jdbcTemplate
                            .query(forwardWithCatsSQL,
                                   new Object[]{feedName, markerEntry.getCreationDate(),
                                           CategoryStringGenerator.getPostgresCategoryString(searchString), pageSize},
                                   new EntryRowMapper());
                } else {
                    feedPage = jdbcTemplate
                            .query(forwardSQL,
                                   new Object[]{feedName, markerEntry.getCreationDate(), pageSize},
                                   new EntryRowMapper());
                }
                Collections.reverse(feedPage);
                break;

            case BACKWARD:

                final String backwardSQL = "SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? ORDER BY datelastupdated DESC LIMIT ?";
                final String backwardWithCatsSQL = "SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? AND categories @> ?::varchar[] ORDER BY datelastupdated DESC LIMIT ?";

                if (searchString.length() > 0) {
                    feedPage = jdbcTemplate
                            .query(backwardWithCatsSQL,
                                   new Object[]{feedName, markerEntry.getCreationDate(),
                                           CategoryStringGenerator.getPostgresCategoryString(searchString), pageSize},
                                   new EntryRowMapper());
                } else {
                    feedPage = jdbcTemplate
                            .query(backwardSQL,
                                   new Object[]{feedName, markerEntry.getCreationDate(), pageSize},
                                   new EntryRowMapper());
                }
                break;
        }

        return feedPage;
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

