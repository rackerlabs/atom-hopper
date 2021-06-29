package org.atomhopper.dynamodb.query;

public enum SearchType {
    FEED_FORWARD,
    FEED_BACKWARD,
    FEED_HEAD,
    LAST_PAGE,
    NEXT_LINK,
    BY_TIMESTAMP_FORWARD,
    BY_TIMESTAMP_BACKWARD
}
