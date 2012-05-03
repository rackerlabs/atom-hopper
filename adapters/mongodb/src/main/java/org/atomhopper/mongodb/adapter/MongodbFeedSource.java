package org.atomhopper.mongodb.adapter;

import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Order;
import org.springframework.data.mongodb.core.query.Query;

public class MongodbFeedSource implements FeedSource {

    private static final int PAGE_SIZE = 25;
    private static final String LAST_ENTRY = "last";
    private static final String DATE_LAST_UPDATED = "dateLastUpdated";
    private static final String FEED = "feed";
    private static final String ID = "_id";
    private MongoTemplate mongoTemplate;

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void setParameters(Map<String, String> params) {
    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries, GetFeedRequest getFeedRequest, final int pageSize) {
        final Feed hyrdatedFeed = abdera.newFeed();
        // Get the last page of entries
        Query query = new Query(Criteria.where(FEED).is(getFeedRequest.getFeedName())).limit(1);
        query.sort().on(DATE_LAST_UPDATED, Order.ASCENDING);
        final PersistedEntry lastPersistedEntry = mongoTemplate.findOne(query, PersistedEntry.class);

        if (!(persistedEntries.isEmpty())) {
            hyrdatedFeed.setId(persistedEntries.get(0).getFeed());
            hyrdatedFeed.setTitle(persistedEntries.get(0).getFeed());

            hyrdatedFeed.addLink(new StringBuilder().append(decode(getFeedRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))).append("entries/").append(lastPersistedEntry.getEntryId()).toString()).setRel(LAST_ENTRY);

            // If limit > actual number of entries in the database, there
            // is not a previous or next link
            if (persistedEntries.size() > pageSize) {
                // Set the previous link
                final String BASE_FEED_URI = decode(getFeedRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));

                hyrdatedFeed.addLink(new StringBuilder()
                        .append(BASE_FEED_URI)
                        .append("?marker=")
                        .append(persistedEntries.get(0).getEntryId())
                        .append("&limit=")
                        .append(String.valueOf(pageSize))
                        .append("&direction=forward").toString())
                        .setRel(Link.REL_PREVIOUS);

                // Set the next link
                hyrdatedFeed.addLink(new StringBuilder()
                        .append(BASE_FEED_URI)
                        .append("?marker=")
                        .append(persistedEntries.get(persistedEntries.size() - 1).getEntryId())
                        .append("&limit=")
                        .append(String.valueOf(pageSize))
                        .append("&direction=backward").toString())
                        .setRel(Link.REL_NEXT);
                // If the amount of persisted entries is greater than the pageSize
                // then remove the last persisted entry and set the next link to
                // the last entry
                if (persistedEntries.size() > pageSize) {
                    persistedEntries.remove(persistedEntries.size() - 1);
                }
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
            Query queryForFeedHead = new Query(Criteria.where(FEED).is(feedName)).limit(pageSize + 1);
            queryForFeedHead.sort().on(DATE_LAST_UPDATED, Order.DESCENDING);

            SimpleCategoryCriteriaGenerator simpleCategoryCriteriaGenerator = new SimpleCategoryCriteriaGenerator(searchString);
            simpleCategoryCriteriaGenerator.enhanceCriteria(queryForFeedHead);
            final List<PersistedEntry> persistedEntries = mongoTemplate.find(queryForFeedHead, PersistedEntry.class);

            response = ResponseBuilder.found(hydrateFeed(abdera, persistedEntries, getFeedRequest, pageSize));
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
                    getFeedRequest.getFeedName(), markerEntry, pageDirection, new SimpleCategoryCriteriaGenerator(searchString), pageSize + 1),
                    getFeedRequest, pageSize);

            response = ResponseBuilder.found(feed);
        } else {
            response = ResponseBuilder.notFound("No entry with specified marker found");
        }

        return response;
    }

    private List<PersistedEntry> enhancedGetFeedPage(final String feedName, final PersistedEntry markerEntry, final PageDirection direction, final CategoryCriteriaGenerator criteriaGenerator, final int pageSize) {

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
                feedPage.addFirst(markerEntry);
                break;
        }

        return feedPage;
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
