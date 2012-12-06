package org.atomhopper.jdbc.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import org.atomhopper.jdbc.model.PersistedEntry;

import org.springframework.jdbc.core.ResultSetExtractor;

public class EntryResultSetExtractor implements ResultSetExtractor {

    @Override
    public Object extractData(ResultSet rs) throws SQLException {

        PersistedEntry entry = new PersistedEntry();
        entry.setSqlId(rs.getLong("id"));
        entry.setFeed(rs.getString("feed"));
        entry.setCreationDate(rs.getTimestamp("creationdate"));
        entry.setDateLastUpdated(rs.getTimestamp("datelastupdated"));
        entry.setEntryBody(rs.getString("entrybody"));
        entry.setEntryId(rs.getString("entryid"));

        Object[] objArray = (Object[]) rs.getArray("categories").getArray();
        entry.setCategories(Arrays.copyOf(objArray, objArray.length, String[].class));
        return entry;
    }
}
