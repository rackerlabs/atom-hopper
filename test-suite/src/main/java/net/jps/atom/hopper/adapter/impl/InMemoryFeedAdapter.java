package net.jps.atom.hopper.adapter.impl;

import net.jps.atom.hopper.adapter.FeedPublisher;
import net.jps.atom.hopper.adapter.FeedSource;
import net.jps.atom.hopper.adapter.ResponseBuilder;
import net.jps.atom.hopper.adapter.request.*;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import net.jps.atom.hopper.util.uri.template.EnumKeyedTemplateParameters;
import net.jps.atom.hopper.util.uri.template.URITemplate;
import org.apache.abdera.model.Categories;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;

import java.util.Calendar;
import java.util.Collections;
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
    public AdapterResponse<Entry> getEntry(GetEntryRequest getEntryRequest) {
        if (!StringUtils.isBlank(getEntryRequest.getId())) {
            final AtomEntry entry = liveFeed.get(getEntryRequest.getId());

            if (entry != null) {
                return ResponseBuilder.found(entry.getEntry());
            }
        }

        return ResponseBuilder.notFound();
    }

    @Override
    public AdapterResponse<Feed> getFeed(GetFeedRequest getFeedRequest) {
        final Feed feed = getFeedRequest.getRequestContext().getAbdera().newFeed();

        feed.setTitle("A Feed");
        feed.addLink(getFeedRequest.urlFor(
                new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED))).setRel("self");

        for (AtomEntry ae : liveFeed.values()) {
            feed.addEntry(ae.getEntry());
        }

        return ResponseBuilder.found(feed);
    }

    @Override
    public AdapterResponse<Categories> getCategories(GetCategoriesRequest getCategoriesRequest) {
        return ResponseBuilder.found(getCategoriesRequest.newCategories());
    }

    @Override
    public Feed getFeedByDateRange(Calendar startingEntryDate, Calendar lastEntryDate) {
        throw new UnsupportedOperationException("Not supported yet.");
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
