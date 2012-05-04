package org.atomhopper.hibernate.adapter;

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
import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.dbal.PageDirection;
import org.atomhopper.hibernate.query.SimpleCategoryCriteriaGenerator;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;
import org.apache.abdera.model.Link;

public class HibernateFeedSource implements FeedSource {

    private static final int PAGE_SIZE = 25;
    private FeedRepository feedRepository;
    private static final String LAST_ENTRY = "last";

    public void setFeedRepository(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    @Override
    public FeedInformation getFeedInformation() {
        return new HibernateFeedInformation(feedRepository);
    }

    @Override
    public void setParameters(Map<String, String> params) {
    }

    private Feed hydrateFeed(Abdera abdera, List<PersistedEntry> persistedEntries, GetFeedRequest getFeedRequest, final int pageSize) {
        final Feed hyrdatedFeed = abdera.newFeed();

        if (!(persistedEntries.isEmpty())) {
            final String BASE_FEED_URI = decode(getFeedRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)));
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";

            hyrdatedFeed.setId(UUID.randomUUID().toString());
            hyrdatedFeed.setTitle(getFeedRequest.getFeedName().toString());

            // Set the previous link
            hyrdatedFeed.addLink(new StringBuilder().append(BASE_FEED_URI).append("?marker=").append(persistedEntries.get(0).getEntryId()).append("&limit=").append(String.valueOf(pageSize)).append("&search=").append(searchString).append("&direction=forward").toString()).setRel(Link.REL_PREVIOUS);

            // If limit > actual number of entries in the database, there
            // is not a next link
            if (persistedEntries.size() > pageSize) {
                // Set the next link
                hyrdatedFeed.addLink(new StringBuilder().append(BASE_FEED_URI).append("?marker=").append(persistedEntries.get(persistedEntries.size() - 1).getEntryId()).append("&limit=").append(String.valueOf(pageSize)).append("&search=").append(searchString).append("&direction=backward").toString()).setRel(Link.REL_NEXT);
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
        final PersistedEntry entry = feedRepository.getEntry(getEntryRequest.getEntryId(), getEntryRequest.getFeedName());
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

        final PersistedFeed persistedFeed = feedRepository.getFeed(getFeedRequest.getFeedName());
        final PersistedEntry markerEntry = feedRepository.getEntry(marker, getFeedRequest.getFeedName());

        if (markerEntry != null) {
            final String searchString = getFeedRequest.getSearchQuery() != null ? getFeedRequest.getSearchQuery() : "";
            final Feed feed = hydrateFeed(getFeedRequest.getAbdera(),
                    feedRepository.getFeedPage(getFeedRequest.getFeedName(), markerEntry, pageDirection,
                    new SimpleCategoryCriteriaGenerator(searchString), pageSize), getFeedRequest, pageSize);

            response = ResponseBuilder.found(feed);
        } else {
            response = ResponseBuilder.notFound("No entry with specified marker found");
        }

        return response;
    }
}
