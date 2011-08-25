package org.atomhopper.hibernate.adapter;

import java.io.IOException;
import java.io.StringWriter;
import org.atomhopper.dbal.FeedRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.abdera.model.Entry;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.jpa.Category;
import org.atomhopper.adapter.jpa.Feed;
import org.atomhopper.adapter.jpa.FeedEntry;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateFeedPublisher implements FeedPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateFeedPublisher.class);
    
    private FeedRepository feedRepository;

    public void setFeedRepository(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry abderaParsedEntry = postEntryRequest.getEntry();
        final FeedEntry domainFeedEntry = new FeedEntry();

        domainFeedEntry.setCategories(processCategories(abderaParsedEntry.getCategories(), domainFeedEntry));
        domainFeedEntry.setEntryId(UUID.randomUUID().toString());

        final Feed feedRef = new Feed(postEntryRequest.getFeedName());

        domainFeedEntry.setFeed(feedRef);
        domainFeedEntry.setEntryBody(entryToString(abderaParsedEntry));

        abderaParsedEntry.setId(domainFeedEntry.getEntryId());

        feedRepository.saveEntry(domainFeedEntry);

        return ResponseBuilder.created(abderaParsedEntry);
    }

    private Set<Category> processCategories(List<org.apache.abdera.model.Category> abderaCategories, FeedEntry feedEntryRef) {
        final Set<Category> entryCategories = new HashSet<Category>();
        final Set<FeedEntry> entrySet = new HashSet<FeedEntry>();
        entrySet.add(feedEntryRef);

        for (org.apache.abdera.model.Category abderaCat : abderaCategories) {
            entryCategories.add(new Category(abderaCat.getTerm()));
        }

        return entryCategories;
    }

    private String entryToString(Entry entry) {
        final StringWriter writer = new StringWriter();

        try {
            entry.writeTo(writer);
        } catch (IOException ioe) {
            LOG.error("Unable to write entry to string. Unable to persist entry. Reason: " + ioe.getMessage(), ioe);
            
            throw new PublicationException(ioe.getMessage(), ioe);
        }

        return writer.toString();
    }

    @Override
    @NotImplemented
    public AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    @NotImplemented
    public AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
