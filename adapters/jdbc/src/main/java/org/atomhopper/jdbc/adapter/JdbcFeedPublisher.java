package org.atomhopper.jdbc.adapter;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.TimerContext;
import org.apache.abdera.model.Categories;
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
import org.atomhopper.jdbc.query.PostgreSQLTextArray;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.response.EmptyBody;
import org.atomhopper.util.uri.template.EnumKeyedTemplateParameters;
import org.atomhopper.util.uri.template.URITemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.abdera.i18n.text.UrlEncoding.decode;

/**
 * Implements the FeedPublisher interface for writing to a postgres datastore and implements the following:
 *
 * <ul>
 *     <li>Populates a PersistedEntry instance to be written to the database</li>
 *     <li>Records performance metrics</li>
 *     <li>Supports overriding the timestamp</li>
 *     <li>Supports overriding the id</li>
 *     <li>Insert categories with predefined prefixes to specified columns for better search performance</li>
 *     <li>Insert specified categories into the generic categories column as well as to the specified column
 *     for migration purposes</li>
 * </ul>
 *
 * Mapping category prefixes to postgres columns is done through the following:
 * <ul>
 *     <li>PrefixColumnMap - maps a prefix key to a column name.  E.g., 'tid' to 'tenantid'</li>
 *     <li>Delimiter - used to extract the prefix from a category.  E.g., if the delimiter is ':' the category
 *     value would be 'tid:1234'</li>
 *     <li>AsCategorySet - prefixes listed here are saved to the corresponding column as well as in the generic
 *     categories column.  This is used for migrating a category from the generic column to the specific column</li>
 * </ul>
 */
public class JdbcFeedPublisher implements FeedPublisher, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger( JdbcFeedPublisher.class );
    private static final String UUID_URI_SCHEME = "urn:uuid:";
    private static final String LINKREL_SELF = "self";
    private JdbcTemplate jdbcTemplate;

    private boolean allowOverrideId = false;
    private boolean allowOverrideDate = false;
    private boolean enableTimers = false;

    private Map<String, String> mapPrefix = new HashMap<String, String>();
    private Set<String> setBothSet = new HashSet<String>();

    private String split;

    private Map<String, Counter> counterMap = Collections.synchronizedMap( new HashMap<String, Counter>() );

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

    protected JdbcTemplate getJdbcTemplate() {

        return jdbcTemplate;
    }

    public void setAsCategorySet( Set<String> set ) {

        setBothSet = new HashSet<String>( set );
    }

    public void setPrefixColumnMap( Map<String, String> prefix ) {

        mapPrefix = new HashMap<String, String>( prefix );
    }

    public void setDelimiter( String splitParam ) {

        split = splitParam;
    }

    @Override
    @NotImplemented
    public void setParameters(Map<String, String> params) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public void afterPropertiesSet() {

        if( split != null ^ !mapPrefix.isEmpty() ) {

            throw new IllegalArgumentException( "The 'delimiter' and 'prefixColumnMap' field must both be defined" );
        }
    }

    private String createSql( String insertSql1, String insertSql2 ) {
        String insertSqlEnd = ")";

        StringBuilder sbSql = new StringBuilder();
        sbSql.append( insertSql1 );

        for( String prefix : mapPrefix.keySet() ) {

            sbSql.append( ", " + mapPrefix.get( prefix ) );
        }

        sbSql.append( insertSql2 );

        for( int i = 0; i < mapPrefix.size(); i++ ) {

            sbSql.append( ", ?" );
        }

        sbSql.append( insertSqlEnd );
        return sbSql.toString();
    }

    private void insertDbOverrideDate( PersistedEntry persistedEntry ) {

        String insertSql1 = "INSERT INTO entries (entryid, creationdate, datelastupdated, entrybody, feed, categories";
        String insertSql2 = ") VALUES (?, ?, ?, ?, ?, ?";

        String sql = createSql( insertSql1, insertSql2 );

        Categories categories = new Categories( persistedEntry.getCategories() );

        List<Object> params = new ArrayList<Object>();
        params.add( persistedEntry.getEntryId() );
        params.add( persistedEntry.getCreationDate() );
        params.add( persistedEntry.getDateLastUpdated() );
        params.add( persistedEntry.getEntryBody() );
        params.add( persistedEntry.getFeed() );
        params.add( new PostgreSQLTextArray( categories.getCategories() ) );

        for( String prefix : mapPrefix.keySet() ) {

            params.add( categories.getPrefix( prefix ) );
        }

        getJdbcTemplate().update(sql, params.toArray( new Object[0] ));
    }

    private void insertDb( PersistedEntry persistedEntry ) {

        Categories categories = new Categories( persistedEntry.getCategories() );

        String insertSql1 = "INSERT INTO entries (entryid, entrybody, feed, categories";
        String insertSql2 = ") VALUES (?, ?, ?, ?";

        String sql = createSql( insertSql1, insertSql2 );

        List<Object> params = new ArrayList<Object>();
        params.add( persistedEntry.getEntryId() );
        params.add( persistedEntry.getEntryBody() );
        params.add( persistedEntry.getFeed() );
        params.add( new PostgreSQLTextArray( categories.getCategories() ) );

        for( String prefix : mapPrefix.keySet() ) {

            params.add( categories.getPrefix( prefix ) );
        }

        getJdbcTemplate().update( sql, params.toArray( new Object[0] ));
    }

    @Override
    public AdapterResponse<Entry> postEntry(PostEntryRequest postEntryRequest) {
        final TimerContext context = startTimer("post-entry");

        try {
            final Entry abderaParsedEntry = postEntryRequest.getEntry();
            final PersistedEntry persistedEntry = new PersistedEntry();


            boolean entryIdSent = abderaParsedEntry.getId() != null;
            if (allowOverrideId && entryIdSent && StringUtils.isNotBlank( abderaParsedEntry.getId().toString().trim() )) {
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
                if ( allowOverrideDate ) {

                    insertDbOverrideDate( persistedEntry );

                } else {

                    insertDb( persistedEntry );
                }

            } catch (DuplicateKeyException dupEx) {
                String errMsg = String.format("Unable to persist entry. Reason: entryId (%s) not unique.", abderaParsedEntry.getId().toString());
                return ResponseBuilder.conflict( errMsg );
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

    private void incrementCounterForFeed(String feedName) {

        if (!counterMap.containsKey(feedName)) {
            synchronized (counterMap) {
                if (!counterMap.containsKey(feedName)) {
                    Counter counter = Metrics.newCounter( JdbcFeedPublisher.class, "entries-created-for-" + feedName );
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

    class Categories {

        private String[] categories = new String[ 0 ];

        private Map<String, String> mapByPrefix = new HashMap<String, String>();

        public Categories( String[] cats ) {

            List<String> list = new ArrayList<String>();

            for( String cat : cats ) {

                boolean isPrefix = false;

                for( String prefix : mapPrefix.keySet() ) {

                    String prefixSplit =  prefix + split;

                    if( cat.startsWith( prefixSplit ) ) {

                        mapByPrefix.put( prefix, cat.substring( prefixSplit.length() ) );

                        // if we are setting both, we want it in the column as well as the generic categories array
                        if( !setBothSet.contains( prefix ) )
                            isPrefix = true;

                        break;
                    }
                }

                if( !isPrefix ) {
                    list.add( cat );
                }
            }

            categories = list.toArray( categories );
        }

        public String getPrefix( String prefix ) {

            return mapByPrefix.get( prefix );
        }

        public String[] getCategories() {
            return categories;
        }

    }
}
