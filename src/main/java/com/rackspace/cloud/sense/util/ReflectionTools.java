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

import java.lang.reflect.Constructor;
import org.apache.log4j.Logger;

/**
 *
 * @author zinic
 */
public class ReflectionTools {

    private static final Logger localLogger = Logger.getLogger(StaticLoggingFacade.class);

    public static Class getCallerClassFromStackTrace(int depth) {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        if (stackTrace != null && stackTrace.length > depth) {
            final String className = stackTrace[depth].getClassName();

            try {
                return Class.forName(className);
            } catch (ClassNotFoundException cnfe) {
                localLogger.error("Error finding class for logging: " + className);
            }
        }

        return null;
    }

    public static <T> T construct(Class<T> c, Object... parameters) {
        try {
//            final Constructor<T> constructor = c.getConstructor(toClassArray(parameters));
            final Constructor<T> constructor = getConstructor(c, toClassArray(parameters));

            if (constructor != null) {
                return constructor.newInstance(parameters);
            }

            localLogger.error("No constructors for class " + c.getCanonicalName() + " answer to constructor");
        } catch (Exception instanciationException) {
            localLogger.error("Failed to create new instance of class: " + c.getCanonicalName(), instanciationException);
        }

        throw new RuntimeException("Failed to create new exception instance for class: " + c.getCanonicalName());
    }

    private static <T> Constructor<T> getConstructor(Class<T> c, Class[] parameters) throws NoSuchMethodException {
        for (Constructor constructor : c.getConstructors()) {
            final Class[] constructorParameters = constructor.getParameterTypes();

            if (parameters.length != constructorParameters.length) {
                continue;
            }

            boolean suitable = true;

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i] == null) {
                    continue;
                }

                if (!constructorParameters[i].isAssignableFrom(parameters[i])) {
                    suitable = false;
                    break;
                }
            }

            if (suitable) {
                return constructor;
            }
        }

        throw new NoSuchMethodException();
    }

    private static Class[] toClassArray(Object... objects) {
        final Class[] classArray = new Class[objects.length];

        for (int i = 0; i < objects.length; i++) {
            classArray[i] = objects[i] != null ? objects[i].getClass() : null;
        }

        return classArray;
    }
}
