package net.jps.atom.hopper.util.uri.template;

public class URITemplateBuilder {

    private final String host;

    public URITemplateBuilder(String host) {
        this.host = host;
    }

    private void appendWorkspaceTemplateString(StringBuilder templateStringBuilder) {
        templateStringBuilder.append("http://{host=");
        templateStringBuilder.append(host).append("}{-prefix|:|port}{target_base}");
        templateStringBuilder.append("/{workspace}");
    }

    private String buildWorkspaceTemplate() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        return templateStringBuilder.toString();
    }

    private String buildFeedTemplate() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        templateStringBuilder.append("/{feed}");
        templateStringBuilder.append("{-prefix|/entries/|entry}/{-opt|?|lochint,limit}{-join|&|lochint,limit}");

        return templateStringBuilder.toString();
    }

    private String buildCategoriesTempalte() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        templateStringBuilder.append("/{feed}/categories");

        return templateStringBuilder.toString();
    }

    private String buildArchivesTemplate() {
        final StringBuilder templateStringBuilder = new StringBuilder();
        appendWorkspaceTemplateString(templateStringBuilder);

        templateStringBuilder.append("/{feed}/archives/{-prefix|/|year}{-prefix|/|month}{-prefix|/|day}{-prefix|/|time}");

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
