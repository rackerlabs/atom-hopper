package org.atomhopper.util.context;

import org.apache.commons.lang.StringUtils;
import org.atomhopper.servlet.ApplicationContextAdapter;
import org.atomhopper.util.reflection.ReflectionTools;

/**
 *
 * 
 */
public class AdapterGetter {

    private final ApplicationContextAdapter contextAdapter;

    public AdapterGetter(ApplicationContextAdapter contextAdapter) {
        this.contextAdapter = contextAdapter;
    }

    public <T> T getByName(String referenceName, Class<T> classToCastTo) {
        if (StringUtils.isBlank(referenceName)) {
            throw new IllegalArgumentException("Bean reference for an adapter must not be empty or null");
        }

        final Object reference = contextAdapter.fromContext(referenceName, classToCastTo);

        if (reference == null) {
            throw new AdapterNotFoundException("Unable to find adapter by name: " + referenceName);
        } else if (!classToCastTo.isInstance(reference)) {
            throw new IllegalArgumentException("Class: "
                    + reference.getClass().getCanonicalName()
                    + " does not implement " + classToCastTo.getCanonicalName());
        }

        return (T) reference;
    }

    public <T> T getByClassDefinition(Class<?> configuredAdapterClass, Class<T> classToCastTo) {
        if (!classToCastTo.isAssignableFrom(configuredAdapterClass)) {
            throw new IllegalArgumentException("Class: "
                    + configuredAdapterClass.getCanonicalName()
                    + " does not implement " + classToCastTo.getCanonicalName());
        }

        try {
            final T instance = (T) contextAdapter.fromContext(configuredAdapterClass);

            return instance != null
                    ? instance
                    : (T) ReflectionTools.construct(configuredAdapterClass);
        } catch (Exception ex) {
            throw new AdapterConstructionException("Failed to get and or construct class: "
                    + configuredAdapterClass.getCanonicalName(), ex);
        }
    }
}
