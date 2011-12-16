package org.atomhopper.config;

import org.atomhopper.abdera.FeedAdapter;
import org.atomhopper.abdera.WorkspaceHandler;
import org.atomhopper.abdera.WorkspaceProvider;
import org.atomhopper.config.v1_0.Author;
import org.atomhopper.config.v1_0.ConfigurationDefaults;
import org.atomhopper.config.v1_0.FeedConfiguration;

/**
 * User: sbrayman
 * Date: 12/13/11
 * <p/>
 * This class preprocesses the WorkspaceProvider configuration and set all default values possible.
 */
public class WorkspaceProviderConfigurationPreprocessor {

    public static void setDefaults(WorkspaceProvider workspaceProvider, ConfigurationDefaults configDefaults) {

        for (WorkspaceHandler handler : workspaceProvider.getWorkspaceManager().getHandlers()) {
            if (handler.getMyConfig().getDefaults() == null) {
                handler.getMyConfig().setDefaults(configDefaults);
            }

            Author author = null;
            for (Object feedAdapter : handler.getCollectionAdapterMapValues()) {
                author = ((FeedAdapter) feedAdapter).getFeedConfiguration().getAuthor();
                if (author == null || author.getName() == null || author.getName().isEmpty()) {
                    ((FeedAdapter) feedAdapter).getFeedConfiguration().setAuthor(configDefaults.getAuthor());
                }
            }

            for (FeedConfiguration feedConfiguration : handler.getMyConfig().getFeed()) {
                if (feedConfiguration.getAuthor() == null || feedConfiguration.getAuthor().getName() == null || feedConfiguration.getAuthor().getName().isEmpty()) {
                    feedConfiguration.setAuthor(configDefaults.getAuthor());
                }
            }
        }
    }
}
