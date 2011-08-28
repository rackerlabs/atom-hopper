package org.atomhopper.hibernate.adapter;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import org.apache.abdera.Abdera;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.response.AdapterResponse;
import org.apache.abdera.model.Document;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.request.RequestQueryParameter;
import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.dbal.PageDirection;

public class HibernateFeedSource implements FeedSource {

    private FeedRepository feedRepository;

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

    private Feed hydrateFeed(Abdera abdera, PersistedFeed persistedFeed, List<PersistedEntry> persistedEntries) {
        final Feed hyrdatedFeed = abdera.newFeed();

        hyrdatedFeed.setId(persistedFeed.getFeedId());
        hyrdatedFeed.setTitle(persistedFeed.getName());

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
        final PersistedEntry entry = feedRepository.getEntry(getEntryRequest.getEntryId());
        AdapterResponse<Entry> response = ResponseBuilder.notFound();

        if (entry != null) {
            response = ResponseBuilder.found(hydrateEntry(entry, getEntryRequest.getAbdera()));
        }

        return response;
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        AdapterResponse<Feed> response = ResponseBuilder.notFound();

        int pageSize = 25;

        try {
            final String pageSizeString = getFeedRequest.getPageSize();

            if (StringUtils.isNotBlank(pageSizeString)) {
                pageSize = Integer.parseInt(pageSizeString);
            }
        } catch (NumberFormatException nfe) {
            return ResponseBuilder.badRequest("Page size parameter not valid");
        }

        final String marker = getFeedRequest.getPageMarker();

        if (StringUtils.isNotBlank(marker)) {
            response = getFeedPage(getFeedRequest, marker, pageSize);
        } else {
            response = getFeedHead(getFeedRequest.getAbdera(), getFeedRequest.getFeedName(), pageSize);
        }

        return response;
    }

    private AdapterResponse<Feed> getFeedHead(Abdera abdera, String feedName, int pageSize) {
        final PersistedFeed persistedFeed = feedRepository.getFeed(feedName);
        AdapterResponse<Feed> response = ResponseBuilder.notFound();

        if (persistedFeed != null) {
            final List<PersistedEntry> persistedEntries = feedRepository.getFeedHead(feedName, pageSize);

            response = ResponseBuilder.found(hydrateFeed(abdera, persistedFeed, persistedEntries));
        }

        return response;
    }

    private AdapterResponse<Feed> getFeedPage(GetFeedRequest getFeedRequest, String marker, int pageSize) {
        AdapterResponse<Feed> response = ResponseBuilder.notFound();
        PageDirection pageDirection = null;

        try {
            final String pageDirectionValue = getFeedRequest.getRequestParameter(RequestQueryParameter.PAGE_DIRECTION.toString());
            pageDirection = PageDirection.valueOf(pageDirectionValue.toUpperCase());
        } catch (Exception iae) {
            return ResponseBuilder.badRequest("Marker must have a page direction specified as either \"forward\" or \"backward\"");
        }

        final PersistedFeed persistedFeed = feedRepository.getFeed(getFeedRequest.getFeedName());
        final PersistedEntry markerEntry = feedRepository.getEntry(marker);

        if (markerEntry != null) {
            final Feed feed = hydrateFeed(getFeedRequest.getAbdera(), persistedFeed, feedRepository.getFeedPage(getFeedRequest.getFeedName(), markerEntry, pageSize, pageDirection));

            response = ResponseBuilder.found(feed);
        } else {
            response = ResponseBuilder.notFound("No entry with specified marker found");
        }

        return response;
    }
}
