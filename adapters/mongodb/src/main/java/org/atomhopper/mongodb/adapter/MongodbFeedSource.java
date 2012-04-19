package org.atomhopper.mongodb.adapter;

import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;

public class MongodbFeedSource implements FeedSource {

    private static final Logger LOG = LoggerFactory.getLogger(MongodbFeedSource.class);
    private static final int PAGE_SIZE = 25;
    private static final String LAST_ENTRY = "last";
    private static final String DATE_LAST_UPDATED = "dateLastUpdated";
    private MongoTemplate mongoTemplate;

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void setParameters(Map<String, String> params) {
    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries, GetFeedRequest getFeedRequest) {
        final Feed hyrdatedFeed = abdera.newFeed();
        Query query = new Query(Criteria.where("feed").is(getFeedRequest.getFeedName()));
        query.sort().on(DATE_LAST_UPDATED, Order.ASCENDING);
        final PersistedEntry persistedEntry = mongoTemplate.findOne(query, PersistedEntry.class);

        if (!(persistedEntries.isEmpty())) {
            hyrdatedFeed.setId(persistedEntries.get(0).getFeed());
            hyrdatedFeed.setTitle(persistedEntries.get(0).getFeed());

            hyrdatedFeed.addLink(new StringBuilder().append(decode(getFeedRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))).append("entries/").append(persistedEntry.getEntryId()).toString()).setRel(LAST_ENTRY);
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
                Criteria.where("feed").is(getEntryRequest.getFeedName()).andOperator(Criteria.where("id").is(getEntryRequest.getEntryId()))), PersistedEntry.class);


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
        Query queryIfFeedExists = new Query(Criteria.where("feed").is(feedName));
        final PersistedEntry persistedEntry = mongoTemplate.findOne(queryIfFeedExists, PersistedEntry.class);

        AdapterResponse<Feed> response = null;

        if (persistedEntry != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            final List<PersistedEntry> feedHead = new LinkedList<PersistedEntry>();
            Query queryForFeedHead = new Query(Criteria.where("feed").is(feedName)).limit(pageSize);
            queryForFeedHead.sort().on(DATE_LAST_UPDATED, Order.ASCENDING);

            SimpleCategoryCriteriaGenerator simpleCategoryCriteriaGenerator = new SimpleCategoryCriteriaGenerator(searchString);
            simpleCategoryCriteriaGenerator.enhanceCriteria(queryForFeedHead);
            final List<PersistedEntry> persistedEntries = mongoTemplate.find(queryForFeedHead, PersistedEntry.class);

            response = ResponseBuilder.found(hydrateFeed(abdera, persistedEntries, getFeedRequest));
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
                Criteria.where("feed").is(getFeedRequest.getFeedName()).andOperator(Criteria.where("id").is(getFeedRequest.getFeedName()))), PersistedEntry.class);

        if (markerEntry != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            final Feed feed = hydrateFeed(
                    getFeedRequest.getAbdera(),
                    enhancedGetFeedPage(
                            getFeedRequest.getFeedName(), markerEntry, pageDirection, new SimpleCategoryCriteriaGenerator(searchString), pageSize),
                    getFeedRequest);

            response = ResponseBuilder.found(feed);
        } else {
            response = ResponseBuilder.notFound("No entry with specified marker found");
        }

        return response;
    }

    private List<PersistedEntry> enhancedGetFeedPage(final String feedName, final PersistedEntry markerEntry, final PageDirection direction, final CategoryCriteriaGenerator criteriaGenerator, final int pageSize) {

        final LinkedList<PersistedEntry> feedPage = new LinkedList<PersistedEntry>();
        final Query query = new Query(Criteria.where("feed").is(feedName)).limit(pageSize);

        criteriaGenerator.enhanceCriteria(query);

        switch (direction) {
            case FORWARD:
                query.addCriteria(Criteria.where(DATE_LAST_UPDATED).gt(markerEntry.getCreationDate()));
                query.sort().on(DATE_LAST_UPDATED, Order.ASCENDING);
                feedPage.addAll(mongoTemplate.find(query, PersistedEntry.class));
                Collections.reverse(feedPage);
                break;

            case BACKWARD:
                query.addCriteria(Criteria.where(DATE_LAST_UPDATED).gt(markerEntry.getCreationDate()));
                query.sort().on(DATE_LAST_UPDATED, Order.DESCENDING);
                feedPage.add(markerEntry);
                feedPage.addAll(mongoTemplate.find(query, PersistedEntry.class));
                break;
        }

        return feedPage;
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
