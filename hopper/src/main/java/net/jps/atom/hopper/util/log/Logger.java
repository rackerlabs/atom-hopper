package net.jps.atom.hopper.util.log;

/**
 *
 * @author jhopper
 */
public interface Logger {

    void info(String message);

    <T extends Exception> T newException(String message, Class<T> exceptionClass) throws T;

    <T extends Exception> T newException(String message, Throwable cause, Class<T> exceptionClass) throws T;

    <T extends Exception> T wrapError(Throwable ex, Class<T> exceptionWrapperClass) throws T;

    void info(Exception ex);

    void error(Exception ex);
    
}