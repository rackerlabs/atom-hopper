package org.atomhopper.jdbc.query;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class SqlBuilder {
    private String searchString;
    private SearchType type;
    private int feedHeadDelayInSeconds = -1;
    private DateTime startingTimestamp;

    private static final String EQUALS = "=";
    private static final String LESS_THAN = "<";
    private static final String GREATER_THAN = ">";

    private static final String QUESTION_MARK = "?";

    private static final String OPEN_PARENS = "(";
    private static final String CLOSE_PARENS = ")";

    private static final String SELECT = "SELECT * FROM entries WHERE feed = ?";
    private static final String AND = "AND";
    private static final String SPACE = " ";
    private static final String DATELASTUPDATED = "datelastupdated %s ?";
    private static final String ID = "id %s ?";

    private static final String UNION_ALL = "UNION ALL";

    private static final String ORDER_BY_ASC = "ORDER BY datelastupdated ASC, id ASC LIMIT ?";
    private static final String ORDER_BY_ASC_LIMIT = "ORDER BY datelastupdated ASC, id ASC LIMIT %s";
    private static final String ORDER_BY_DATE_ASC_ID_DESC_LIMIT = "ORDER BY datelastupdated ASC, id DESC LIMIT %s";
    private static final String ORDER_BY_DESC_LIMIT = "ORDER BY datelastupdated DESC, id DESC LIMIT %s";
    private static final String ORDER_BY_DATE_DESC_ID_ASC_LIMIT = "ORDER BY datelastupdated DESC, id ASC LIMIT %s";
    private static final String DB_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS z";

    private SearchToSqlConverter searchToSqlConverter;

    public SqlBuilder( SearchToSqlConverter converter ) {

        searchToSqlConverter = converter;
    }

    public SqlBuilder searchString(String searchString) {
        this.searchString = searchString;
        return this;
    }

    public SqlBuilder searchType(SearchType type) {
        this.type = type;
        return this;
    }

    public SqlBuilder feedHeadDelayInSeconds(int delay) {
        this.feedHeadDelayInSeconds = delay;
        return this;
    }

    public SqlBuilder startingTimestamp(DateTime timestamp) {
        this.startingTimestamp = timestamp;
        return this;
    }

    @Override
    public String toString() {

        String searchSql = searchToSqlConverter.getSqlFromSearchString(searchString);
        StringBuilder builder = new StringBuilder();

        switch (type) {
            case FEED_FORWARD:
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, EQUALS));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(ID, GREATER_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                // D-15000: when we are getting feed head and there are
                // aggressive inserts going on at the same time, Postgres
                // does not guarantee that entries that are inserted later
                // will have later timestamps. This is just due to the nature
                // of multi-process and multi-threaded-ness of the database.
                // Therefore, we return only entries that have been inserted
                // in the database n seconds from the current select time.
                if ( feedHeadDelayInSeconds != -1 ) {
                    builder.append(AND);
                    builder.append(" datelastupdated < now() - interval '");
                    builder.append(feedHeadDelayInSeconds);
                    builder.append(" seconds' ");
                }

                builder.append(CLOSE_PARENS);
                builder.append(SPACE + UNION_ALL + SPACE);
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, GREATER_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                // D-15000: when we are getting feed head and there are
                // aggressive inserts going on at the same time, Postgres
                // does not guarantee that entries that are inserted later
                // will have later timestamps. This is just due to the nature
                // of multi-process and multi-threaded-ness of the database.
                // Therefore, we return only entries that have been inserted
                // in the database n seconds from the current select time.
                if ( feedHeadDelayInSeconds != -1 ) {
                    builder.append(AND);
                    builder.append(" datelastupdated < now() - interval '");
                    builder.append(feedHeadDelayInSeconds);
                    builder.append(" seconds' ");
                }

                builder.append(String.format(ORDER_BY_ASC));
                builder.append(CLOSE_PARENS + SPACE);
                builder.append(String.format(ORDER_BY_ASC));

                return builder.toString();

            case FEED_BACKWARD:
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, EQUALS));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(ID, LESS_THAN+EQUALS));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(CLOSE_PARENS);
                builder.append(SPACE + UNION_ALL + SPACE);
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, LESS_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(String.format(ORDER_BY_DESC_LIMIT, QUESTION_MARK));
                builder.append(CLOSE_PARENS + SPACE);
                builder.append(String.format(ORDER_BY_DESC_LIMIT, QUESTION_MARK));

                return builder.toString();

            case FEED_HEAD:
                builder.append(String.format(SELECT));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                // D-15000: when we are getting feed head and there are
                // aggressive inserts going on at the same time, Postgres
                // does not guarantee that entries that are inserted later
                // will have later timestamps. This is just due to the nature
                // of multi-process and multi-threaded-ness of the database.
                // Therefore, we return only entries that have been inserted
                // in the database n seconds from the current select time.
                if ( feedHeadDelayInSeconds != -1 ) {
                    builder.append(AND);
                    builder.append(" datelastupdated < now() - interval '");
                    builder.append(feedHeadDelayInSeconds);
                    builder.append(" seconds' ");
                }

                builder.append(String.format(ORDER_BY_DESC_LIMIT, QUESTION_MARK));

                return builder.toString();

            case NEXT_LINK:
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, EQUALS));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(ID, LESS_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(CLOSE_PARENS);
                builder.append(SPACE + UNION_ALL + SPACE);
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, LESS_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(String.format(ORDER_BY_DESC_LIMIT, 1));
                builder.append(CLOSE_PARENS + SPACE);
                builder.append(String.format(ORDER_BY_DESC_LIMIT, 1));

                return builder.toString();

            case BY_TIMESTAMP_FORWARD:
            case BY_TIMESTAMP_BACKWARD:
                if ( startingTimestamp == null ) {
                    throw new IllegalArgumentException("for searchType " + type + ", startingTimestamp() must be used");
                }
                builder.append(SELECT);
                builder.append(" ");

                if ( feedHeadDelayInSeconds != -1 ) {
                    builder.append(AND);
                    builder.append(" datelastupdated < now() - interval '");
                    builder.append(feedHeadDelayInSeconds);
                    builder.append(" seconds' ");
                }

                DateTimeZone timeZone = startingTimestamp.getZone();

                builder.append(AND);
                builder.append(" (datelastupdated at time zone current_setting('TIMEZONE')) at time zone '");
                builder.append(timeZone.getID());

                if ( type == SearchType.BY_TIMESTAMP_BACKWARD ) {
                    builder.append("' <= '");
                } else {
                    builder.append("' >= '");
                }

                DateTimeFormatter postgresDTF = DateTimeFormat.forPattern(DB_TIMESTAMP_PATTERN);
                builder.append(startingTimestamp.toString(postgresDTF));
                builder.append("'::timestamp ");

                if ( type == SearchType.BY_TIMESTAMP_BACKWARD ) {
                    builder.append(String.format(ORDER_BY_DATE_DESC_ID_ASC_LIMIT, 1));
                } else {
                    builder.append(String.format(ORDER_BY_DATE_ASC_ID_DESC_LIMIT, 1));
                }
                return builder.toString();

            case LAST_PAGE:
            default:
                builder.append(String.format(SELECT));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                // D-15000: when we are getting feed head and there are
                // aggressive inserts going on at the same time, Postgres
                // does not guarantee that entries that are inserted later
                // will have later timestamps. This is just due to the nature
                // of multi-process and multi-threaded-ness of the database.
                // Therefore, we return only entries that have been inserted
                // in the database n seconds from the current select time.
                if ( feedHeadDelayInSeconds != -1 ) {
                    builder.append(AND);
                    builder.append(" datelastupdated < now() - interval '");
                    builder.append(feedHeadDelayInSeconds);
                    builder.append(" seconds' ");
                }

                builder.append(String.format(ORDER_BY_ASC));

                return builder.toString();
        }
    }

}
