package org.atomhopper.jdbc.adapter;

import com.yammer.metrics.core.TimerContext;
import org.apache.abdera.model.Entry;
import org.apache.commons.lang.StringUtils;
import org.atomhopper.adapter.FeedPublisher;
import org.atomhopper.adapter.NotImplemented;
import org.atomhopper.adapter.PublicationException;
import org.atomhopper.adapter.ResponseBuilder;
import org.atomhopper.adapter.request.adapter.DeleteEntryRequest;
import org.atomhopper.adapter.request.adapter.PostEntryRequest;
import org.atomhopper.adapter.request.adapter.PutEntryRequest;
import org.atomhopper.jdbc.model.PersistedEntry;
import org.atomhopper.jdbc.query.EntryRowMapper;
import org.atomhopper.jdbc.query.PostgreSQLTextArray;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;


public class JdbcFeedPublisher implements FeedPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcFeedPublisher.class);
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";
    private JdbcTemplate jdbcTemplate;

    private boolean allowOverrideId = false;
    private boolean allowOverrideDate = false;
    private boolean enableTimers = false;

    private Map<String, Counter> counterMap = Collections.synchronizedMap(new HashMap<String, Counter>());

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setAllowOverrideId(boolean allowOverrideId) {
        this.allowOverrideId = allowOverrideId;
    }

    public void setAllowOverrideDate(boolean allowOverrideDate) {
        this.allowOverrideDate = allowOverrideDate;
    }

    public void setEnableTimers(Boolean enableTimers) {
        this.enableTimers = enableTimers;
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final TimerContext context = startTimer("post-entry");

        try {
            final Entry abderaParsedEntry = postEntryRequest.getEntry();
            final PersistedEntry persistedEntry = new PersistedEntry();
            final String insertSQL = "INSERT INTO entries (entryid, creationdate, datelastupdated, entrybody, feed, categories) VALUES (?, ?, ?, ?, ?, ?)";

            boolean entryIdSent = abderaParsedEntry.getId() != null;

            if (allowOverrideId && entryIdSent && StringUtils.isNotBlank(abderaParsedEntry.getId().toString().trim())) {
                persistedEntry.setEntryId(abderaParsedEntry.getId().toString());
            } else {
                // Generate an ID for this entry
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

            if (abderaParsedEntry.getSelfLink() == null) {
                abderaParsedEntry.addLink(decode(postEntryRequest.urlFor(new EnumKeyedTemplateParameters<URITemplate>(URITemplate.FEED)))
                    + "entries/" + persistedEntry.getEntryId()).setRel(LINKREL_SELF);
            }

            persistedEntry.setFeed(postEntryRequest.getFeedName());
            persistedEntry.setEntryBody(entryToString(abderaParsedEntry));

            abderaParsedEntry.setUpdated(persistedEntry.getDateLastUpdated());
            abderaParsedEntry.setPublished(persistedEntry.getCreationDate());

            final TimerContext dbcontext = startTimer("db-post-entry");
            try {
                jdbcTemplate.update(insertSQL, new Object[]{
                    persistedEntry.getEntryId(), persistedEntry.getCreationDate(), persistedEntry.getDateLastUpdated(),
                    persistedEntry.getEntryBody(), persistedEntry.getFeed(), new PostgreSQLTextArray(persistedEntry.getCategories())
                });
            } catch (DuplicateKeyException dupEx) {
                String errMsg = String.format("Unable to persist entry. Reason: entryId (%s) not unique.", abderaParsedEntry.getId().toString());
                return ResponseBuilder.conflict(errMsg);
            }  finally {
                stopTimer(dbcontext);
            }

            incrementCounterForFeed(postEntryRequest.getFeedName());

            return ResponseBuilder.created(abderaParsedEntry);
        } finally {
            stopTimer(context);
        }
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

    private void incrementCounterForFeed(String feedName) {

        if (!counterMap.containsKey(feedName)) {
            synchronized (counterMap) {
                if (!counterMap.containsKey(feedName)) {
                    Counter counter = Metrics.newCounter(JdbcFeedPublisher.class, "entries-created-for-" + feedName);
                    counterMap.put(feedName, counter);
                }
            }
        }

        counterMap.get(feedName).inc();
    }

    private TimerContext startTimer(String name) {
        if (enableTimers) {
            final com.yammer.metrics.core.Timer timer = Metrics.newTimer(getClass(), name, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
            TimerContext context = timer.time();
            return context;
        } else {
            return null;
        }
    }

    private void stopTimer(TimerContext context) {
        if ( enableTimers && context != null ) {
            context.stop();
        }
    }
}
