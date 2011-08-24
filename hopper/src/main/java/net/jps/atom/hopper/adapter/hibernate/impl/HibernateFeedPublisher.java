package net.jps.atom.hopper.adapter.hibernate.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.jps.atom.hopper.adapter.FeedPublisher;
import net.jps.atom.hopper.adapter.ResponseBuilder;
import net.jps.atom.hopper.adapter.hibernate.impl.domain.Category;
import net.jps.atom.hopper.adapter.hibernate.impl.domain.Feed;
import net.jps.atom.hopper.adapter.hibernate.impl.domain.FeedEntry;
import net.jps.atom.hopper.adapter.request.DeleteEntryRequest;
import net.jps.atom.hopper.adapter.request.PostEntryRequest;
import net.jps.atom.hopper.adapter.request.PutEntryRequest;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import org.apache.abdera.model.Entry;

public class HibernateFeedPublisher implements FeedPublisher {

    private FeedRepository feedRepository;
    private String feedName;
    
    public void setFeedName(String name) {
        feedName = name;
    }
    
    public void setFeedRepository(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }
    
    @Override
    public AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry abderaParsedEntry = postEntryRequest.getEntry();
        final FeedEntry domainFeedEntry = new FeedEntry();
        final Set<Category> entryCategories = new HashSet<Category>();
        
        for (org.apache.abdera.model.Category abderaCat : abderaParsedEntry.getCategories()) {
            final Category domainCategory = new Category();
            domainCategory.setName(abderaCat.getTerm());
            
            entryCategories.add(domainCategory);
        }
        
        domainFeedEntry.setCategories(entryCategories);
        domainFeedEntry.setEntryId(UUID.randomUUID().toString());
        
        final Feed feedRef = new Feed();
        feedRef.setName(feedName);
        
        domainFeedEntry.setFeed(feedRef);
        domainFeedEntry.setEntryBody(abderaParsedEntry.getText());
        
        abderaParsedEntry.setId(domainFeedEntry.getEntryId());
        
        return ResponseBuilder.created(abderaParsedEntry);
    }

    @Override
    public AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
