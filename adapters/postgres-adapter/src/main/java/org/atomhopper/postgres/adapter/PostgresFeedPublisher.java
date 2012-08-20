package org.atomhopper.postgres.adapter;

import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.jpa.PersistedFeed;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import javax.sql.DataSource;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;
import org.apache.abdera.model.Entry;
import org.atomhopper.postgres.model.PersistedEntry;

public class PostgresFeedPublisher implements FeedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresFeedPublisher.class);
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";
    private DataSource dataSource;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
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
        JdbcTemplate insert = new JdbcTemplate(dataSource);

        // Generate an ID for this entry
        persistedEntry.setEntryId(UUID_URI_SCHEME + UUID.randomUUID().toString());
        // Set the categories
        persistedEntry.setCategories(processCategories(abderaParsedEntry.getCategories()));

        // Make sure the persisted xml has the right id
        abderaParsedEntry.setId(persistedEntry.getEntryId());

        abderaParsedEntry.addLink(decode(postEntryRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))
                + "entries/" + persistedEntry.getEntryId()).setRel(LINKREL_SELF);

        persistedEntry.setFeed(postEntryRequest.getFeedName());
        persistedEntry.setEntryBody(entryToString(abderaParsedEntry));

        abderaParsedEntry.setId(persistedEntry.getEntryId());
        abderaParsedEntry.setUpdated(persistedEntry.getDateLastUpdated());

        insert.update("INSERT INTO entries (entryid, creationdate, datelastupdated, entrybody, feed, categories) VALUES (?, ?, ?, ?, ?, ?)", new Object[]{
            persistedEntry.getEntryId(), persistedEntry.getCreationDate(), persistedEntry.getDateLastUpdated(),
            persistedEntry.getEntryBody(), persistedEntry.getFeed(), persistedEntry.getCategories()
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
}
