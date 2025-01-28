package org.atomhopper.dynamodb.query;

import org.joda.time.DateTime;

import java.util.Map;

public class DynamoDBQueryBuilder {
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

    private SQLToNoSqlConverter SQLToNoSqlConverter;

    public DynamoDBQueryBuilder(SQLToNoSqlConverter converter ) {

        SQLToNoSqlConverter = converter;
    }

    public DynamoDBQueryBuilder searchString(String searchString) {
        this.searchString = searchString;
        return this;
    }

    public DynamoDBQueryBuilder searchType(SearchType type) {
        this.type = type;
        return this;
    }

    public DynamoDBQueryBuilder feedHeadDelayInSeconds(int delay) {
        this.feedHeadDelayInSeconds = delay;
        return this;
    }

    public DynamoDBQueryBuilder startingTimestamp(DateTime timestamp) {
        this.startingTimestamp = timestamp;
        return this;
    }

    public String getFilters(Map<String,String> map) {

        String searchSql = SQLToNoSqlConverter.getSqlFromSearchString(searchString,map);

        switch (type) {

            case FEED_BACKWARD:
                return searchSql;
                
            case FEED_FORWARD:
                return searchSql;
        }
        return null;
    }

}
