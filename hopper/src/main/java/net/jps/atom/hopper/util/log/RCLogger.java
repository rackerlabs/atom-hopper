package net.jps.atom.hopper.util.log;

import net.jps.fava.reflection.ReflectionTools;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * The RCLogger is a simple wrapper for the java.util.logging.Logger designed to
 * add a number of 'nice' features and syntax enhancements to the logging metaphor.
 *
 *
 */
public class RCLogger implements Logger {

    private final static Marker SEVERE = MarkerFactory.getMarker("SEVERE");

    private final org.slf4j.Logger loggerRef;

    public RCLogger(Class clazz) {
        loggerRef = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void info(String message) {
        loggerRef.info(message);
    }

    @Override
    public <T extends Exception> T newException(String message, Class<T> exceptionClass) throws T {
        return newException(message, null, exceptionClass);
    }

    @Override
    public <T extends Exception> T newException(String message, Throwable cause, Class<T> exceptionClass) throws T {
        Throwable newExceptionInstance;

        if (cause == null) {
            newExceptionInstance = ReflectionTools.construct(exceptionClass, message);
        } else {
            newExceptionInstance = ReflectionTools.construct(exceptionClass, message, cause);
        }

        loggerRef.error(message, cause) ;

        return (T) newExceptionInstance;
    }

    @Override
    public <T extends Exception> T wrapError(Throwable ex, Class<T> exceptionWrapperClass) throws T {
        return wrap(SEVERE, ex, exceptionWrapperClass);
    }

    @Override
    public void info(Exception ex) {
        loggerRef.info("", ex);
    }

    @Override
    public void error(Exception ex) {
        loggerRef.error("", ex);
    }

    private <T extends Exception> T wrap(Marker severity, Throwable ex, Class<T> exceptionWrapperClass) throws T {
        Exception newExceptionInstance;

        if (!ex.getClass().equals(exceptionWrapperClass)) {
            newExceptionInstance = ReflectionTools.construct(exceptionWrapperClass, ex.getMessage(), ex);
        } else {
            newExceptionInstance = (T) ex;
        }

        loggerRef.error(severity, ex.getMessage(), ex);

        return (T) newExceptionInstance;
    }
}
