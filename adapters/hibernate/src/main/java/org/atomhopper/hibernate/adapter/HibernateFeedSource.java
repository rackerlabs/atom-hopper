package org.atomhopper.hibernate.adapter;

import java.io.StringReader;
import java.util.Calendar;
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
import org.atomhopper.adapter.NotImplemented;
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
    
    private Entry hydrateFeedEntry(PersistedEntry entry, Abdera abderaReference) {
        final Document<Entry> hydratedEntry = abderaReference.getParser().parse(new StringReader(entry.getEntryBody()));

        return hydratedEntry != null ? hydratedEntry.getRoot() : null;
    }

    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        final PersistedEntry entry = feedRepository.getEntry(getEntryRequest.getEntryId());
        AdapterResponse<Entry> response = ResponseBuilder.notFound();

        if (entry != null) {
            response = ResponseBuilder.found(hydrateFeedEntry(entry, getEntryRequest.getAbdera()));
        }

        return response;
    }

    //TODO: decompose 
    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        PageDirection pageDirection = null;
        String marker = null;

        if (StringUtils.isNotBlank(getFeedRequest.getPageMarker())) {
            final String pageDirectionValue = getFeedRequest.getRequestParameter(RequestQueryParameter.PAGE_DIRECTION.toString());

            try {
                pageDirection = PageDirection.valueOf(pageDirectionValue);
            } catch (IllegalArgumentException iae) {
                return ResponseBuilder.badRequest("Marker must have a page direction specified as either \"forward\" or \"backward\"");
            }
        }

        final PersistedFeed persistedFeed = feedRepository.getFeed(getFeedRequest.getFeedName());

        if (persistedFeed != null) {
            final Feed hydratedFeed = getFeedRequest.newFeed();

            hydratedFeed.setId(persistedFeed.getFeedId());
            hydratedFeed.setTitle(persistedFeed.getName());

            for (PersistedEntry persistedFeedEntry : feedRepository.getFeedPage(getFeedRequest.getFeedName(), marker, 25, pageDirection)) {
                hydratedFeed.addEntry(hydrateFeedEntry(persistedFeedEntry, getFeedRequest.getAbdera()));
            }

            return ResponseBuilder.found(hydratedFeed);
        }

        return ResponseBuilder.notFound();
    }
}
