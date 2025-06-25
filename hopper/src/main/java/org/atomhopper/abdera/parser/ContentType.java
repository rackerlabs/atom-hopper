package org.atomhopper.abdera.parser;

public enum ContentType {
    // JSON types
    APPLICATION_JSON("application/json", Type.JSON),
    TEXT_JSON("text/json", Type.JSON),
    VENDOR_JSON("application/vnd.api+json", Type.JSON),
    RACKSPACE_VENDOR_JSON("application/vnd.rackspace.atom+json", Type.JSON),

    // XML types
    APPLICATION_XML("application/xml", Type.XML),
    TEXT_XML("text/xml", Type.XML),
    APPLICATION_ATOM_XML("application/atom+xml", Type.XML);

    private final String value;
    private final Type type;

    private enum Type { JSON, XML }

    ContentType(String value, Type type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public boolean isJson() {
        return type == Type.JSON;
    }

    public boolean isXml() {
        return type == Type.XML;
    }
}

