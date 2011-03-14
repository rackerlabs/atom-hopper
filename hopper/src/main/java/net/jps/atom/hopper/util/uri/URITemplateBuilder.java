package net.jps.atom.hopper.util.uri;

public class URITemplateBuilder {

    private final String host;
    private String workspaceResource, feedResource;

    public URITemplateBuilder(String host) {
        this.host = host;
    }

    public void setFeedResource(String feedResource) {
        this.feedResource = feedResource;
    }

    public void setWorkspaceResource(String workspaceResource) {
        this.workspaceResource = workspaceResource;
    }

    public URITemplate toCategoriesTemplate() {
        throw new UnsupportedOperationException();
    }

    public URITemplate toArchiveTemplate() {
        throw new UnsupportedOperationException();
    }

    public URITemplate toFeedTemplate() {
        final StringBuilder templateString = new StringBuilder("http://{host=");
        templateString.append(host).append("}{-prefix|:|port}{target_base}");
        templateString.append("/{workspace=").append(workspaceResource).append("}");
        templateString.append("/{feed=").append(feedResource).append("}");
        templateString.append("{-prefix|/entries/|entry}/{-opt|?|lochint,limit}{-join|&|lochint,limit}");

        return new URITemplate(templateString.toString());
    }
}
