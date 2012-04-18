package org.atomhopper.mongodb.adapter;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.apache.abdera.Abdera;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;


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

    private Feed hydrateFeed(Abdera abdera, PersistedFeed persistedFeed, List<PersistedEntry> persistedEntries, GetFeedRequest getFeedRequest) {
        final Feed hyrdatedFeed = abdera.newFeed();

        hyrdatedFeed.setId(persistedFeed.getFeedId());
        hyrdatedFeed.setTitle(persistedFeed.getName());

        final PersistedEntry persistedEntry = mongoTemplate.findOne(new Query(
                Criteria.where("feed").is(getFeedRequest.getFeedName())
                .andOperator(Criteria.where("id")
                .is(getFeedRequest.getEntryId()))), PersistedEntry.class);

        //mongoTemplate.findOne(new Sort(new Sort.Order(Sort.Direction.ASC, DATE_LAST_UPDATED)));

        if (!(persistedEntries.isEmpty())) {
            hyrdatedFeed.addLink(decode(getFeedRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))
                    + "entries/" + feedRepository.getLastEntry(persistedFeed.getName()).getEntryId()).setRel(LAST_ENTRY);
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
                Criteria.where("feed").is(getEntryRequest.getFeedName())
                .andOperator(Criteria.where("id")
                .is(getEntryRequest.getEntryId()))), PersistedEntry.class);


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
        final PersistedFeed persistedFeed = feedRepository.getFeed(feedName);
        AdapterResponse<Feed> response = null;

        if (persistedFeed != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            final List<PersistedEntry> persistedEntries = feedRepository.getFeedHead(feedName, new SimpleCategoryCriteriaGenerator(searchString), pageSize);

            response = ResponseBuilder.found(hydrateFeed(abdera, persistedFeed, persistedEntries, getFeedRequest));
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

        final PersistedFeed persistedFeed = feedRepository.getFeed(getFeedRequest.getFeedName());
        final PersistedEntry markerEntry = feedRepository.getEntry(marker, getFeedRequest.getFeedName());

        if (markerEntry != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            final Feed feed = hydrateFeed(
                    getFeedRequest.getAbdera(), persistedFeed,
                    feedRepository.getFeedPage(
                    getFeedRequest.getFeedName(), markerEntry, pageDirection, new SimpleCategoryCriteriaGenerator(searchString), pageSize),
                    getFeedRequest);

            response = ResponseBuilder.found(feed);
        } else {
            response = ResponseBuilder.notFound("No entry with specified marker found");
        }

        return response;
    }

    @Override
    public FeedInformation getFeedInformation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
