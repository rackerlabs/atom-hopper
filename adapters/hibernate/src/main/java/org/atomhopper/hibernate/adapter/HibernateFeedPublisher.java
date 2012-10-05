package org.atomhopper.hibernate.adapter;

import org.apache.abdera.model.Entry;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.jpa.PersistedCategory;
import org.atomhopper.adapter.jpa.PersistedEntry;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.dbal.FeedRepository;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;


public class HibernateFeedPublisher implements FeedPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateFeedPublisher.class);
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";

    private boolean allowOverrideId = false;
    private boolean allowOverrideDate = false;

    private FeedRepository feedRepository;

    public void setFeedRepository(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
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

        // Update our category indicies
        final Set<PersistedCategory> entryCategories = feedRepository.updateCategories(processCategories(abderaParsedEntry.getCategories()));
        persistedEntry.setCategories(entryCategories);

        boolean entryIdSent = abderaParsedEntry.getId() != null;

        // Generate an ID for this entry
        if (allowOverrideId && entryIdSent) {
            String entryId = abderaParsedEntry.getId().toString();
            // Check to see if entry with this id already exists
            PersistedEntry exists = feedRepository.getEntry(entryId, postEntryRequest.getFeedName());
            if (exists != null) {
                String errMsg = String.format("Unable to persist entry. Reason: entryId (%s) not unique.", entryId);
                return ResponseBuilder.error(errMsg);
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

        abderaParsedEntry.addLink(decode(postEntryRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))
                                          + "entries/" + persistedEntry.getEntryId()).setRel(LINKREL_SELF);

        final PersistedFeed feedRef = new PersistedFeed(postEntryRequest.getFeedName(), UUID_URI_SCHEME + UUID.randomUUID().toString());

        persistedEntry.setFeed(feedRef);
        persistedEntry.setEntryBody(entryToString(abderaParsedEntry));

        abderaParsedEntry.setUpdated(persistedEntry.getDateLastUpdated());

        feedRepository.saveEntry(persistedEntry);

        return ResponseBuilder.created(abderaParsedEntry);
    }

    private Set<PersistedCategory> processCategories(List<org.apache.abdera.model.Category> abderaCategories) {
        final Set<PersistedCategory> entryCategories = new HashSet<PersistedCategory>();

        for (org.apache.abdera.model.Category abderaCat : abderaCategories) {
            entryCategories.add(new PersistedCategory(abderaCat.getTerm().toLowerCase()));
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
