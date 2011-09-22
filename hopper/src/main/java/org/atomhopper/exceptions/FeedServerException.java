package org.atomhopper.exceptions;

class FeedServerException extends RuntimeException {

    public FeedServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public FeedServerException(String message) {
        super(message);
    }
}
