package org.atomhopper.config;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.atomhopper.config.v1_0.*;

/**
 * @author zinic
 */
public class AtomHopperConfigurationPreprocessor {

    private final Configuration configuration;

    public AtomHopperConfigurationPreprocessor(Configuration configuration) {
        this.configuration = configuration;
    }

    public AtomHopperConfigurationPreprocessor applyDefaults() {
        final ConfigurationDefaults configurationDefaults = configuration.getDefaults();
        setDefaultAuthor(configuration.getWorkspace(), configurationDefaults.getAuthor());

        return new AtomHopperConfigurationPreprocessor(configuration);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    private void setDefaultAuthor(List<WorkspaceConfiguration> workspaces, Author globalAuthorDefault) {
        for (WorkspaceConfiguration workspaceConfiguration : workspaces) {
            final Author workspaceAuthorDefault = (workspaceConfiguration.getDefaults() == null)
                    ? null : workspaceConfiguration.getDefaults().getAuthor();

            final Author authorToApply = isAuthorEmpty(workspaceAuthorDefault)
                    ? globalAuthorDefault : workspaceAuthorDefault;

            if (!isAuthorEmpty(authorToApply)) {
                for (FeedConfiguration feed : workspaceConfiguration.getFeed()) {
                    if (isAuthorEmpty(feed.getAuthor())) {
                        feed.setAuthor(globalAuthorDefault);
                    }
                }
            }
        }
    }

    private boolean isAuthorEmpty(Author author) {
        if (author == null) {
            return true;
        } else {
            return author.getName() == null || StringUtils.isBlank(author.getName());
        }
    }
}
