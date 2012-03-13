package org.atomhopper.util.uri.template;

public enum URITemplate {

    WORKSPACE("{scheme=}://{host=}{-prefix|:|port}{target_base}/{workspace}"),
    FEED(WORKSPACE.toString() + "/{feed}{-prefix|/entries/|entry}/{-opt|?|lochint,limit}{-join|&|lochint,limit}");

    //Class Contents
    private final String templateString;

    private URITemplate(String templateString) {
        this.templateString = templateString;
    }

    @Override
    public String toString() {
        return templateString;
    }
}
