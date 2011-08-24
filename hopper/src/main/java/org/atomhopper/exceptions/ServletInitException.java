package org.atomhopper.exceptions;

import javax.servlet.ServletException;

public class ServletInitException extends ServletException {

    public ServletInitException(String message) {
        super(message);
    }

    public ServletInitException(String message, Throwable cause) {
        super(message, cause);
    }
}
