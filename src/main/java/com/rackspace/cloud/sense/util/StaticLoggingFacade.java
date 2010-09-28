/*
 *  Copyright 2010 zinic.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.rackspace.cloud.sense.util;

import com.rackspace.cloud.sense.util.ReflectionTools;
import org.apache.log4j.Logger;

/**
 *
 * @author zinic
 */
public final class StaticLoggingFacade {

    private static final Logger localLogger = Logger.getLogger(StaticLoggingFacade.class);
    public static final int TRACE_CALL_DEPTH = 2;

    public static enum Severity {

        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL;
        public static final Severity DEFAULT = ERROR;
    }

    private StaticLoggingFacade() {
    }

    public static void logDebug(String message) {
        log(Severity.DEBUG, message);
    }

    public static void logInfo(String message) {
        log(Severity.INFO, message);
    }

    public static void logWarn(String message) {
        log(Severity.WARN, message);
    }

    public static void logError(String message) {
        log(Severity.ERROR, message);
    }

    public static void logFatal(String message) {
        log(Severity.FATAL, message);
    }

    public static <T extends Exception> T newException(String message, Class<T> exceptionClass) throws T {
        return newException(message, null, exceptionClass);
    }

    public static <T extends Exception> T newException(String message, Throwable cause, Class<T> exceptionClass) throws T {
        Throwable newExceptionInstance = cause;

        if (cause == null) {
            newExceptionInstance = ReflectionTools.construct(exceptionClass, message);
        } else {
            newExceptionInstance = ReflectionTools.construct(exceptionClass, message, cause);
        }

        log(Severity.DEFAULT, message, cause);

        return (T) newExceptionInstance;
    }

    public static <T extends Exception> T wrap(Exception ex, Class<T> exceptionWrapperClass) throws T {
        return wrap(Severity.DEFAULT, ex, exceptionWrapperClass);
    }

    public static <T extends Exception> T wrapDebug(Exception ex, Class<T> exceptionWrapperClass) throws T {
        return wrap(Severity.DEBUG, ex, exceptionWrapperClass);
    }

    public static <T extends Exception> T wrapInfo(Exception ex, Class<T> exceptionWrapperClass) throws T {
        return wrap(Severity.INFO, ex, exceptionWrapperClass);
    }

    public static <T extends Exception> T wrapWarn(Exception ex, Class<T> exceptionWrapperClass) throws T {
        return wrap(Severity.WARN, ex, exceptionWrapperClass);
    }

    public static <T extends Exception> T wrapError(Exception ex, Class<T> exceptionWrapperClass) throws T {
        return wrap(Severity.ERROR, ex, exceptionWrapperClass);
    }

    public static <T extends Exception> T wrapFatal(Exception ex, Class<T> exceptionWrapperClass) throws T {
        return wrap(Severity.FATAL, ex, exceptionWrapperClass);
    }

    public static <T extends Exception> T wrap(Severity severity, Exception ex, Class<T> exceptionWrapperClass) throws T {
        Exception newExceptionInstance = ex;

        if (!ex.getClass().equals(exceptionWrapperClass)) {
            newExceptionInstance = ReflectionTools.construct(exceptionWrapperClass, ex.getMessage(), ex);
        }

        log(severity, ex.getMessage(), ex);

        return (T) newExceptionInstance;
    }

    private static Logger getLoggerFromStackTrace() {
        final Class c = ReflectionTools.getCallerClassFromStackTrace(TRACE_CALL_DEPTH);

        return c != null ? Logger.getLogger(c) : localLogger;
    }


    private static void log(Severity severity, String message) {
        final Logger logger = getLoggerFromStackTrace();

        switch (severity) {
            case DEBUG:
                logger.debug(message);
                break;

            case INFO:
                logger.info(message);
                break;

            case WARN:
                logger.warn(message);
                break;

            case ERROR:
                logger.error(message);
                break;

            case FATAL:
                logger.fatal(message);
                break;
        }
    }

    private static void log(Severity severity, String message, Throwable ex) {
        final Logger logger = getLoggerFromStackTrace();

        switch (severity) {
            case DEBUG:
                logger.debug(message, ex);
                break;

            case INFO:
                logger.info(message, ex);
                break;

            case WARN:
                logger.warn(message, ex);
                break;

            case ERROR:
                logger.error(message, ex);
                break;

            case FATAL:
                logger.fatal(message, ex);
                break;
        }
    }
}
