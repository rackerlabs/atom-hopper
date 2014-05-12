package org.atomhopper.jdbc.adapter;

import org.atomhopper.dbal.PageDirection;
import org.atomhopper.jdbc.model.PersistedEntry;
import org.atomhopper.jdbc.query.*;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.RowMapper;

import java.util.*;

/**
 * Implements the default configuration of AbstractJdbcFeedPublisher.  All categories are stored in the same variable
 * length array.
 */
public class JdbcFeedSource extends AbstractJdbcFeedSource {

    @Override
    protected RowMapper getRowMapper() {

        return new EntryRowMapper();
    }
}

