package org.atomhopper.jdbc.adapter;


import org.atomhopper.jdbc.model.PersistedEntry;
import org.atomhopper.jdbc.query.PostgreSQLTextArray;

/**
 * Implements the default configuration of AbstractJdbcFeedPublisher.  All categories are stored in the same variable
 * length array.
 */
public class JdbcFeedPublisher extends AbstractJdbcFeedPublisher {

    @Override
    protected void insertDb( PersistedEntry persistedEntry ) {
        Object[] params;
        String insertSQL = "INSERT INTO entries (entryid, entrybody, feed, categories) VALUES (?, ?, ?, ?)";
        params = new Object[]{
              persistedEntry.getEntryId(), persistedEntry.getEntryBody(), persistedEntry.getFeed(),
              new PostgreSQLTextArray(persistedEntry.getCategories())
        };
        getJdbcTemplate().update(insertSQL, params);
    }

    @Override
    protected void insertDbOverrideDate( PersistedEntry persistedEntry ) {
        Object[] params;
        // D-15000: use auto generated DB timestamp, except for when allowOverrideDate
        // is set to true
        String insertSQL = "INSERT INTO entries (entryid, creationdate, datelastupdated, entrybody, feed, categories) VALUES (?, ?, ?, ?, ?, ?)";
        params = new Object[]{
              persistedEntry.getEntryId(), persistedEntry.getCreationDate(), persistedEntry.getDateLastUpdated(),
              persistedEntry.getEntryBody(), persistedEntry.getFeed(), new PostgreSQLTextArray(persistedEntry.getCategories())
        };
        getJdbcTemplate().update(insertSQL, params);
    }
}
