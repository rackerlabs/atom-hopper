/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.util.context;

import com.rackspace.cloud.commons.util.StringUtilities;
import com.rackspace.cloud.commons.util.reflection.ReflectionTools;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.adapter.archive.FeedArchiveAdapter;

/**
 *
 * @author zinic
 */
public class AdapterGetter {

    private final ApplicationContextAdapter contextAdapter;

    public AdapterGetter(ApplicationContextAdapter contextAdapter) {
        this.contextAdapter = contextAdapter;
    }

    public FeedArchiveAdapter getFeedArchive(Class<?> feedArchiveAdapterClass) {
        if (!feedArchiveAdapterClass.isAssignableFrom(FeedArchiveAdapter.class)) {
        }

        return getByClassDefinition((Class<? extends FeedArchiveAdapter>) feedArchiveAdapterClass, FeedArchiveAdapter.class);
    }

    public FeedArchiveAdapter getFeedArchive(String beanReferenceName) {
        return getByName(beanReferenceName, FeedArchiveAdapter.class);
    }

    public FeedSourceAdapter getFeedSource(Class<?> feedSourceAdapterClass) {
        if (!feedSourceAdapterClass.isAssignableFrom(FeedSourceAdapter.class)) {
        }

        return getByClassDefinition((Class<? extends FeedSourceAdapter>) feedSourceAdapterClass, FeedSourceAdapter.class);
    }

    public FeedSourceAdapter getFeedSource(String beanReferenceName) {
        return getByName(beanReferenceName, FeedSourceAdapter.class);
    }

    private <T> T getByName(String referenceName, Class<T> classToCastTo) {
        if (StringUtilities.isBlank(referenceName)) {
            throw new IllegalArgumentException("Bean reference for an adapter must not be empty or null");
        }

        final T reference = contextAdapter.fromContext(referenceName, classToCastTo);

        if (reference == null) {
            throw new AdapterNotFoundException("Unable to find adapter by name: " + referenceName);
        }

        return reference;
    }

    private <T> T getByClassDefinition(Class<? extends T> configuredAdapterClass, Class<T> classToCastTo) {
        try {
            final T instance = contextAdapter.fromContext(configuredAdapterClass);

            return instance != null
                    ? instance
                    : (T) ReflectionTools.construct(configuredAdapterClass, new Object[0]);
        } catch (Exception ex) {
            throw new AdapterConstructionException("Failed to get and or construct class: "
                    + configuredAdapterClass.getCanonicalName(), ex);
        }
    }
}
