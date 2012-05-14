package org.atomhopper.mongodb.adapter;

import com.mongodb.DBCollection;
import com.mongodb.MongoException;
import java.io.StringReader;
import java.util.*;
import org.apache.abdera.Abdera;
import static org.apache.abdera.i18n.text.UrlEncoding.decode;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.model.Link;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.mongodb.domain.PersistedEntry;
import org.atomhopper.mongodb.query.CategoryCriteriaGenerator;
import org.atomhopper.mongodb.query.SimpleCategoryCriteriaGenerator;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;

public class MongodbFeedSource implements FeedSource {

    private static final int PAGE_SIZE = 25;
    private static final String DATE_LAST_UPDATED = "dateLastUpdated";
    private static final String FEED = "feed";
    private static final String ID = "_id";
    private MongoTemplate mongoTemplate;
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String PERSISTED_ENTRY_COLLECTION = "persistedentry";

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void setParameters(Map<String, String> params) {
    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries, GetFeedRequest getFeedRequest, final int pageSize) {
        final Feed hyrdatedFeed = abdera.newFeed();

        if (!(persistedEntries.isEmpty())) {
            final String BASE_FEED_URI = decode(getFeedRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

            hyrdatedFeed.setId(UUID_URI_SCHEME + UUID.randomUUID().toString());
            hyrdatedFeed.setTitle(persistedEntries.get(0).getFeed());

            // Set the previous link
            hyrdatedFeed.addLink(new StringBuilder()
                    .append(BASE_FEED_URI)
                    .append("?marker=")
                    .append(persistedEntries.get(0).getEntryId())
                    .append("&limit=")
                    .append(String.valueOf(pageSize))
                    .append("&search=")
                    .append(searchString)
                    .append("&direction=forward").toString())
                    .setRel(Link.REL_PREVIOUS);

            final PersistedEntry lastEntryInCollection = persistedEntries.get(persistedEntries.size() - 1);
            Query nextLinkQuery = new Query(Criteria.where(FEED).is(lastEntryInCollection.getFeed())).limit(1)
                    .addCriteria(Criteria.where(DATE_LAST_UPDATED)
                    .lt(lastEntryInCollection.getDateLastUpdated()));
            nextLinkQuery.sort().on(DATE_LAST_UPDATED, Order.DESCENDING);

            SimpleCategoryCriteriaGenerator simpleCategoryCriteriaGenerator = new SimpleCategoryCriteriaGenerator(searchString);
            simpleCategoryCriteriaGenerator.enhanceCriteria(nextLinkQuery);

            final PersistedEntry nextEntry = mongoTemplate.findOne(nextLinkQuery, PersistedEntry.class);

            if (nextEntry != null) {
                // Set the next link
                hyrdatedFeed.addLink(new StringBuilder()
                        .append(BASE_FEED_URI)
                        .append("?marker=")
                        .append(nextEntry.getEntryId())
                        .append("&limit=")
                        .append(String.valueOf(pageSize))
                        .append("&search=")
                        .append(searchString)
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
        final Document<Entry> hydratedEntryDocument = abderaReference.getParser().parse(new StringReader(persistedEntry.getEntryBody()));
        Entry entry = null;

        if (hydratedEntryDocument != null) {
            entry = hydratedEntryDocument.getRoot();

            entry.setUpdated(persistedEntry.getDateLastUpdated());
        }

        return entry;
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        final PersistedEntry entry = mongoTemplate.findOne(new Query(
                Criteria.where(FEED).is(getEntryRequest.getFeedName()).andOperator(Criteria.where(ID).is(getEntryRequest.getEntryId()))), PersistedEntry.class);


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
            response = getFeedHead(getFeedRequest, getFeedRequest.getFeedName(), pageSize);
        }

        return response;
    }

    private AdapterResponse<Feed> getFeedHead(GetFeedRequest getFeedRequest, String feedName, int pageSize) {
        final Abdera abdera = getFeedRequest.getAbdera();
        Query queryIfFeedExists = new Query(Criteria.where(FEED).is(feedName));
        final PersistedEntry persistedEntry = mongoTemplate.findOne(queryIfFeedExists, PersistedEntry.class);

        AdapterResponse<Feed> response = null;

        if (persistedEntry != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            Query queryForFeedHead = new Query(Criteria.where(FEED).is(feedName)).limit(pageSize);
            queryForFeedHead.sort().on(DATE_LAST_UPDATED, Order.DESCENDING);

            SimpleCategoryCriteriaGenerator simpleCategoryCriteriaGenerator = new SimpleCategoryCriteriaGenerator(searchString);
            simpleCategoryCriteriaGenerator.enhanceCriteria(queryForFeedHead);
            final List<PersistedEntry> persistedEntries = mongoTemplate.find(queryForFeedHead, PersistedEntry.class);

            Feed hyrdatedFeed = hydrateFeed(abdera, persistedEntries, getFeedRequest, pageSize);
            // Set the last link in the feed head
            final String BASE_FEED_URI = decode(getFeedRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));
            Query feedQuery = new Query(Criteria.where(FEED).is(getFeedRequest.getFeedName()));
            final int totalFeedEntryCount = safeLongToInt(countDocuments(PERSISTED_ENTRY_COLLECTION, feedQuery) % pageSize);
            int lastPageSize = pageSize;
            if(totalFeedEntryCount != 0) {
                lastPageSize = totalFeedEntryCount;
            }
            Query lastLinkQuery = new Query(Criteria.where(FEED).is(getFeedRequest.getFeedName()))
                    .limit(lastPageSize);
            simpleCategoryCriteriaGenerator.enhanceCriteria(lastLinkQuery);
            lastLinkQuery.sort().on(DATE_LAST_UPDATED, Order.ASCENDING);
            final List<PersistedEntry> lastPersistedEntries = mongoTemplate.find(lastLinkQuery, PersistedEntry.class);

            if (lastPersistedEntries != null && !(lastPersistedEntries.isEmpty())) {
                hyrdatedFeed.addLink(new StringBuilder()
                        .append(BASE_FEED_URI)
                        .append("?marker=")
                        .append(lastPersistedEntries.get(lastPersistedEntries.size() - 1).getEntryId())
                        .append("&limit=")
                        .append(String.valueOf(pageSize))
                        .append("&search=")
                        .append(searchString)
                        .append("&direction=backward").toString())
                        .setRel(Link.REL_LAST);
            }

            response = ResponseBuilder.found(hyrdatedFeed);
        }

        return response != null ? response : ResponseBuilder.found(abdera.newFeed());
    }

    private AdapterResponse<Feed> getFeedPage(GetFeedRequest getFeedRequest, String marker, int pageSize) {
        AdapterResponse<Feed> response;
        PageDirection pageDirection;
        try {
            final String pageDirectionValue = getFeedRequest.getDirection();
            pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());
        } catch (Exception iae) {
            return ResponseBuilder.badRequest("Marker must have a page direction specified as either \"forward\" or \"backward\"");
        }
        final PersistedEntry markerEntry = mongoTemplate.findOne(new Query(
                Criteria.where(FEED).is(getFeedRequest.getFeedName()).andOperator(Criteria.where(ID).is(marker))), PersistedEntry.class);

        if (markerEntry != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            final Feed feed = hydrateFeed(
                    getFeedRequest.getAbdera(),
                    enhancedGetFeedPage(
                    getFeedRequest.getFeedName(), markerEntry, pageDirection, new SimpleCategoryCriteriaGenerator(searchString), pageSize),
                    getFeedRequest, pageSize);

            response = ResponseBuilder.found(feed);
        } else {
            response = ResponseBuilder.notFound("No entry with specified marker found");
        }

        return response;
    }

    private List<PersistedEntry> enhancedGetFeedPage(final String feedName, final PersistedEntry markerEntry,
            final PageDirection direction, final CategoryCriteriaGenerator criteriaGenerator, final int pageSize) {

        final LinkedList<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();
        final Query query = new Query(Criteria.where(FEED).is(feedName)).limit(pageSize);

        criteriaGenerator.enhanceCriteria(query);

        switch (direction) {
            case FORWARD:
                query.addCriteria(Criteria.where(DATE_LAST_UPDATED).gt(markerEntry.getCreationDate()));
                query.sort().on(DATE_LAST_UPDATED, Order.ASCENDING);
                feedPage.addAll(mongoTemplate.find(query, PersistedEntry.class));
                Collections.reverse(feedPage);
                break;

            case BACKWARD:
                query.addCriteria(Criteria.where(DATE_LAST_UPDATED).lte(markerEntry.getCreationDate()));
                query.sort().on(DATE_LAST_UPDATED, Order.DESCENDING);
                feedPage.addAll(mongoTemplate.find(query, PersistedEntry.class));
                break;
        }

        return feedPage;
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private long countDocuments( final String collection, final Query query ) {
        return mongoTemplate.execute( collection,
            new CollectionCallback< Long >() {
                @Override
                public Long doInCollection(DBCollection collection)
                        throws MongoException, DataAccessException {
                    return collection.count(query.getQueryObject());
                }
            }
        );
    }

    private int safeLongToInt(long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (value + " cannot be cast to int without changing its value.");
        }
        return (int) value;
    }
}
