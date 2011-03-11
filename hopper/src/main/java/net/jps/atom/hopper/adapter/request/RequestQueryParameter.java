package net.jps.atom.hopper.adapter.request;

public enum RequestQueryParameter {

    CATEGORIES("categories"),
    MARKER("marker");

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
