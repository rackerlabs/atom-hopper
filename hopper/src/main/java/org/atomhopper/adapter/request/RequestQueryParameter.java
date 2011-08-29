package org.atomhopper.adapter.request;

public enum RequestQueryParameter {

    CATEGORIES("categories"),
    SEARCH("search"),
    PAGE_DIRECTION("direction"),
    MARKER("marker"),
    PAGE_LIMIT("limit");

    //Class contents
    private final String stringValue;

    private RequestQueryParameter(String stringValue) {
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
