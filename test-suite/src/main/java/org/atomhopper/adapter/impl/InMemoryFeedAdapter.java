package org.atomhopper.adapter.impl;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 */
public class InMemoryFeedAdapter implements FeedSource, FeedPublisher {

    private final SortedMap<String, AtomEntry> liveFeed;

    public InMemoryFeedAdapter() {
        liveFeed = Collections.synchronizedSortedMap(new TreeMap<String, AtomEntry>());
    }

    @Override
    public FeedInformation getFeedInformation() {
        return DisabledFeedInformation.getInstance();
    }

    @Override
    public void setParameters(Map<String, String> params) {
    }
    
    @Override
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        if (!StringUtils.isBlank(getEntryRequest.getEntryId())) {
            final AtomEntry entry = liveFeed.get(getEntryRequest.getEntryId());

            if (entry != null) {
                return ResponseBuilder.found(entry.getEntry());
            }
        }

        return ResponseBuilder.notFound();
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        final Feed feed = getFeedRequest.getAbdera().newFeed();

        feed.setTitle("A Feed");
        feed.addLink(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED))).setRel("self");

        for (AtomEntry ae : liveFeed.values()) {
            feed.addEntry(ae.getEntry());
        }

        return ResponseBuilder.found(feed);
    }

    @Override
    public AdapterResponse<EmptyBody> deleteEntry(DeleteEntryRequest deleteEntryRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry entryToPost = postEntryRequest.getEntry();

        if (entryToPost.getId() == null) {
            return ResponseBuilder.reply(HttpStatus.BAD_REQUEST, "Entry should supply an id");
        } else {
            liveFeed.put(entryToPost.getId().toString(), new AtomEntry(entryToPost));
        }

        return ResponseBuilder.created(entryToPost);
    }

    @Override
    public AdapterResponse<Entry> putEntry(PutEntryRequest putEntryRequest) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
