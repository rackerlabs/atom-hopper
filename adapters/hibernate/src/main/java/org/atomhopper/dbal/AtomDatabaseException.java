package org.atomhopper.dbal;

public class AtomDatabaseException extends RuntimeException {

    public AtomDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public AtomDatabaseException(String message) {
        super(message);
    }
}
