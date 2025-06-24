package org.atomhopper.abdera.parser;

public enum ContentType {
    APPLICATION_JSON("application/json", Category.JSON),
    TEXT_JSON("text/json", Category.JSON),
    VENDOR_JSON("application/vnd.api+json", Category.JSON),
    RACKSPACE_VENDOR_JSON("application/vnd.rackspace.atom+json", Category.JSON),

    APPLICATION_XML("application/xml", Category.XML),
    TEXT_XML("text/xml", Category.XML),
    APPLICATION_ATOM_XML("application/atom+xml", Category.XML);

    private final String value;
    private final Category category;

    ContentType(String value, Category category) {
        this.value = value;
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public Category getCategory() {
        return category;
    }

    public enum Category {
        JSON, XML
    }
}

