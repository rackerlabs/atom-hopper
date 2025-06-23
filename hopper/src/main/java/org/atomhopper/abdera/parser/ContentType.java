package org.atomhopper.abdera.parser;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ContentType {
    APPLICATION_JSON("application/json"),
    TEXT_JSON("text/json"),
    VENDOR_JSON("application/vnd.api+json"),

    // XML types
    APPLICATION_XML("application/xml"),
    TEXT_XML("text/xml"),
    APPLICATION_ATOM_XML("application/atom+xml");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
