package org.atomhopper.adapter.request.adapter.impl;

public class RequestParsingException extends RuntimeException {

    public RequestParsingException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public RequestParsingException(String string) {
        super(string);
    }
}
