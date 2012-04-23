package org.atomhopper.mongodb.domain;


public class PersistedCategory {

    private String term;

    public PersistedCategory(String term) {
        this.term = term;
    }

    public String getValue() {
        return this.term;
    }

    public void setValue(String term) {
        this.term = term;
    }
}
