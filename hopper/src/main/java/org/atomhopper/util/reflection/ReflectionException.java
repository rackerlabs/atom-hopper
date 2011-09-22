package org.atomhopper.util.reflection;

/**
 *
 * 
 */
class ReflectionException extends RuntimeException {

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReflectionException(String message) {
        super(message);
    }
}
