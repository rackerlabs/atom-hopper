package org.atomhopper.postgres.adapter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;
import org.apache.abdera.model.Entry;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.postgres.model.PersistedEntry;
import org.atomhopper.postgres.query.EntryRowMapper;
import org.atomhopper.postgres.query.PostgreSQLTextArray;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;


public class PostgresFeedPublisher implements FeedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresFeedPublisher.class);
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";
    private JdbcTemplate jdbcTemplate;

    private boolean allowOverrideId = false;
    private boolean allowOverrideDate = false;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        final String insertSQL = "INSERT INTO entries (entryid, creationdate, datelastupdated, entrybody, feed, categories) VALUES (?, ?, ?, ?, ?, ?)";

        boolean entryIdSent = abderaParsedEntry.getId() != null;

        // Generate an ID for this entry
        if (allowOverrideId && entryIdSent) {
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

        // Set the categories
        persistedEntry.setCategories(processCategories(abderaParsedEntry.getCategories()));

        abderaParsedEntry.addLink(decode(postEntryRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))
                + "entries/" + persistedEntry.getEntryId()).setRel(LINKREL_SELF);

        persistedEntry.setFeed(postEntryRequest.getFeedName());
        persistedEntry.setEntryBody(entryToString(abderaParsedEntry));

        abderaParsedEntry.setUpdated(persistedEntry.getDateLastUpdated());

        jdbcTemplate.update(insertSQL, new Object[]{
            persistedEntry.getEntryId(), persistedEntry.getCreationDate(), persistedEntry.getDateLastUpdated(),
            persistedEntry.getEntryBody(), persistedEntry.getFeed(), new PostgreSQLTextArray(persistedEntry.getCategories())
        });

        return ResponseBuilder.created(abderaParsedEntry);
    }

    private String[] processCategories(List<org.apache.abdera.model.Category> abderaCategories) {
        final List<String> categoriesList = new ArrayList<String>();

        for (org.apache.abdera.model.Category abderaCat : abderaCategories) {
            categoriesList.add(abderaCat.getTerm().toLowerCase());
        }

        final String[] categoryArray = new String[categoriesList.size()];
        categoriesList.toArray(categoryArray);

        return categoryArray;
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

    private PersistedEntry getEntry(final String entryId, final String feedName) {
        final String entrySQL = "SELECT * FROM entries WHERE feed = ? AND entryid = ?";
        List<PersistedEntry> entry = jdbcTemplate
                .query(entrySQL, new Object[]{feedName, entryId}, new EntryRowMapper());
        return entry.size() > 0 ? entry.get(0) : null;
    }
}
