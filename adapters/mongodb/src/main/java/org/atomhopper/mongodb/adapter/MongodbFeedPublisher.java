package org.atomhopper.mongodb.adapter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.mongodb.domain.PersistedCategory;
import org.atomhopper.mongodb.domain.PersistedEntry;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;
import static org.atomhopper.mongodb.adapter.MongodbUtilities.formatCollectionName;


public class MongodbFeedPublisher implements FeedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(MongodbFeedPublisher.class);
    private static final String ID = "_id";
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";
    private MongoTemplate mongoTemplate;

    private boolean allowOverrideId = false;
    private boolean allowOverrideDate = false;

    private Map<String, Counter> counterMap = Collections.synchronizedMap(new HashMap<String, Counter>());

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void setAllowOverrideId(boolean allowOverrideId) {
        this.allowOverrideId = allowOverrideId;
    }

    public void setAllowOverrideDate(boolean allowOverrideDate) {
        this.allowOverrideDate = allowOverrideDate;
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry abderaParsedEntry = postEntryRequest.getEntry();
        final PersistedEntry persistedEntry = new PersistedEntry();

        boolean entryIdSent = abderaParsedEntry.getId() != null;

        // Generate an ID for this entry
        if (allowOverrideId && entryIdSent && StringUtils.isNotBlank(abderaParsedEntry.getId().toString().trim())) {
            String entryId = abderaParsedEntry.getId().toString();
            // Check to see if entry with this id already exists
            PersistedEntry exists = getEntry(entryId, postEntryRequest.getFeedName());
            if (exists != null) {
                String errMsg = String.format("Unable to persist entry. Reason: entryId (%s) not unique.", entryId);
                throw new PublicationException(errMsg);
            }
            persistedEntry.setEntryId(abderaParsedEntry.getId().toString());
        } else {
            persistedEntry.setEntryId(UUID_URI_SCHEME + UUID.randomUUID().toString());
            abderaParsedEntry.setId(persistedEntry.getEntryId());
        }

        if (allowOverrideDate) {
            Date updated = abderaParsedEntry.getUpdated();

            if (updated != null) {
                persistedEntry.setDateLastUpdated(updated);
                persistedEntry.setCreationDate(updated);
            }
        }

        if (abderaParsedEntry.getSelfLink() == null) {
            abderaParsedEntry.addLink(new StringBuilder()
                .append(decode(postEntryRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED))))
                .append("entries/")
                .append(persistedEntry.getEntryId()).toString()).setRel(LINKREL_SELF);
        }

        persistedEntry.setFeed(postEntryRequest.getFeedName());

        for (Category category : (List<Category>) abderaParsedEntry.getCategories()) {
            persistedEntry.addCategory(new PersistedCategory(category.getTerm().toLowerCase()));
        }

        persistedEntry.setEntryBody(entryToString(abderaParsedEntry));

        abderaParsedEntry.setId(persistedEntry.getEntryId());
        abderaParsedEntry.setUpdated(persistedEntry.getDateLastUpdated());
        abderaParsedEntry.setPublished(persistedEntry.getCreationDate());

        mongoTemplate.save(persistedEntry, formatCollectionName(postEntryRequest.getFeedName()));

        incrementCounterForFeed(postEntryRequest.getFeedName());

        return ResponseBuilder.created(abderaParsedEntry);
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

    private PersistedEntry getEntry(String entryId, String feedName) {
        final PersistedEntry entry = mongoTemplate.findOne(new Query(
                Criteria.where(ID).is(entryId)),PersistedEntry.class, formatCollectionName(feedName));
        return entry;
    }

    private void incrementCounterForFeed(String feedName) {

        if (!counterMap.containsKey(feedName)) {
            synchronized (counterMap) {
                if (!counterMap.containsKey(feedName)) {
                    Counter counter = Metrics.newCounter(MongodbFeedPublisher.class, "events-created-for-" + feedName);
                    counterMap.put(feedName, counter);
                }
            }
        }

        counterMap.get(feedName).inc();
    }
}
