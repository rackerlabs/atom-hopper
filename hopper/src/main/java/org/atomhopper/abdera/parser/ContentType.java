package org.atomhopper.abdera.parser;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ContentType {
    // JSON types
    APPLICATION_JSON("application/json"),
    TEXT_JSON("text/json"),
    VENDOR_JSON("application/vnd.api+json"),
    ANY_JSON("+json"),

    // XML types
    APPLICATION_XML("application/xml"),
    TEXT_XML("text/xml"),
    APPLICATION_ATOM_XML("application/atom+xml"),
    ANY_XML("+xml");

    private final String value;
    private static final Set<String> JSON_TYPES = Stream.of(
                    APPLICATION_JSON, TEXT_JSON, VENDOR_JSON, ANY_JSON).map(ContentType::getValue).collect(Collectors.toSet());

    private static final Set<String> XML_TYPES = Stream.of(
                    APPLICATION_XML, TEXT_XML, APPLICATION_ATOM_XML, ANY_XML).map(ContentType::getValue).collect(Collectors.toSet());

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static boolean isJson(String contentType) {
        if (contentType == null) return false;
        String normalized = contentType.toLowerCase().trim();
        return JSON_TYPES.stream().anyMatch(normalized::contains);
    }

    public static boolean isXml(String contentType) {
        if (contentType == null) return false;
        String normalized = contentType.toLowerCase().trim();
        return XML_TYPES.stream().anyMatch(normalized::contains);
    }
}
