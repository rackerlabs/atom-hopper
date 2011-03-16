package net.jps.atom.hopper.util.uri.template;

public class URITemplateBuilder {

    private final String host;
    private String workspaceResource, feedResource;

    public URITemplateBuilder(String host) {
        this.host = host;
    }

    public void setWorkspaceResource(String workspaceResource) {
        this.workspaceResource = workspaceResource;
    }

    public void setFeedResource(String feedResource) {
        this.feedResource = feedResource;
    }

    private void appendWorkspaceTemplateString(StringBuilder templateStringBuilder) {
        templateStringBuilder.append("http://{host=");
        templateStringBuilder.append(host).append("}{-prefix|:|port}{target_base}");
        templateStringBuilder.append("/{workspace=").append(workspaceResource).append("}");
    }

    private String buildWorkspaceTemplate() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        return templateStringBuilder.toString();
    }

    private String buildFeedTemplate() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        templateStringBuilder.append("/{feed=").append(feedResource).append("}");
        templateStringBuilder.append("{-prefix|/entries/|entry}/{-opt|?|lochint,limit}{-join|&|lochint,limit}");

        return templateStringBuilder.toString();
    }

    private String buildCategoriesTempalte() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        templateStringBuilder.append("/{feed=").append(feedResource).append("}");
        templateStringBuilder.append("/categories");

        return templateStringBuilder.toString();
    }

    private String buildArchivesTemplate() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        templateStringBuilder.append("/{feed=").append(feedResource).append("}");
        templateStringBuilder.append("/archives/{-prefix|/|year}{-prefix|/|month}{-prefix|/|day}{-prefix|/|time}");

        return templateStringBuilder.toString();
    }

    public URITemplate toWorkspaceTemplate() {
        return new URITemplate(buildWorkspaceTemplate());
    }

    public URITemplate toCategoriesTemplate() {
        return new URITemplate(buildCategoriesTempalte());
    }

    public URITemplate toArchivesTemplate() {
        return new URITemplate(buildArchivesTemplate());
    }

    public URITemplate toFeedTemplate() {
        return new URITemplate(buildFeedTemplate());
    }
}
