package org.atomhopper.dynamodb.constant;

/**
 * Constant file for all the constant declared in the module.
 */
public final class DynamoDBConstant {
    public static final String UUID_URI_SCHEME = "urn:uuid:";
    public static final String LINK_REL_SELF = "self";
    public static final String ENTRIES = "entries";
    public static final String ENTRY_ID_FEED_INDEX = "entryId-feed-index";
    public static final String ENTRY_ID = "entryId";
    public static final String FEED = "feed";
    public static final int PAGE_SIZE=25;
    public static final String MOCK_LAST_MARKER = "last";
    public static final String MARKER_EQ = "?marker=";
    public static final String LIMIT_EQ = "?limit=";
    public static final String AND_SEARCH_EQ = "&search=";
    public static final String AND_LIMIT_EQ = "&limit=";
    public static final String AND_MARKER_EQ = "&marker=";
    public static final String AND_DIRECTION_EQ = "&direction=";
    public static final String AND_DIRECTION_EQ_BACKWARD = "&direction=backward";
    public static final String AND_DIRECTION_EQ_FORWARD = "&direction=forward";
    public static final String DATE_LAST_UPDATED="dateLastUpdated";
    private DynamoDBConstant() {
    }
}
