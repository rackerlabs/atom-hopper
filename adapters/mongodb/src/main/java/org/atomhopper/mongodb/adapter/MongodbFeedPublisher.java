package org.atomhopper.mongodb.adapter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.apache.abdera.i18n.text.UrlEncoding.decode;
import org.apache.abdera.model.Category;
import org.apache.abdera.model.Entry;
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

public class MongodbFeedPublisher implements FeedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(MongodbFeedPublisher.class);
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";
    private MongoTemplate mongoTemplate;

    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void setParameters(Map<String, String> params) {
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final Entry abderaParsedEntry = postEntryRequest.getEntry();
        final PersistedEntry persistedEntry = new PersistedEntry();

        // Generate an ID for this entry
        persistedEntry.setEntryId(UUID_URI_SCHEME + UUID.randomUUID().toString());

        // Make sure the persisted xml has the right id
        abderaParsedEntry.setId(persistedEntry.getEntryId());

        abderaParsedEntry.addLink(decode(postEntryRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))
                + "entries/" + persistedEntry.getEntryId()).setRel(LINKREL_SELF);

        // TODO: ADd a unique feed id back, probably in the FeedPagingProcessor...
        //final PersistedFeed feedRef = new PersistedFeed(postEntryRequest.getFeedName(), UUID_URI_SCHEME + UUID.randomUUID().toString());
        // persistedEntry.setFeed(feedRef);

        persistedEntry.setFeed(postEntryRequest.getFeedName());

        for (Category category : (List<Category>) abderaParsedEntry.getCategories()) {
            persistedEntry.addCategory(new PersistedCategory(category.getTerm()));
        }

        persistedEntry.setEntryBody(entryToString(abderaParsedEntry));

        abderaParsedEntry.setId(persistedEntry.getEntryId());
        abderaParsedEntry.setUpdated(persistedEntry.getDateLastUpdated());

        mongoTemplate.save(persistedEntry);

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
}
