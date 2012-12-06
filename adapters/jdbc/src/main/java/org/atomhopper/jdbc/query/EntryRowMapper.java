package org.atomhopper.jdbc.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class EntryRowMapper implements RowMapper {
    @Override
    public Object mapRow(ResultSet rs, int line) throws SQLException {
        EntryResultSetExtractor extractor = new EntryResultSetExtractor();
        return extractor.extractData(rs);
    }
}
