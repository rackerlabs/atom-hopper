package net.jps.atom.hopper.util.uri.template;

public enum URITemplateParameter {

    HOST_DOMAIN("host"),
    HOST_PORT("port"),
    WORKSPACE_RESOURCE("workspace"),
    FEED_RESOURCE("feed"),
    ENTRY_RESOURCE("entry"),
    MARKER("lochint"),
    PAGE_LIMIT("limit");

    //Class contents
    private final String stringRepresentation;

    private URITemplateParameter(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    @Override
    public String toString() {
        return stringRepresentation;
    }
}
