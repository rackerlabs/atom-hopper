package org.atomhopper.config;

import org.atomhopper.config.process.AtomHopperConfigurationPreprocessor;
import org.atomhopper.config.v1_0.Author;
import org.atomhopper.config.v1_0.Configuration;
import org.atomhopper.config.v1_0.ConfigurationDefaults;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.config.v1_0.WorkspaceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: sbrayman
 * Date: 12/20/11
 */

@RunWith(Enclosed.class)
public class AtomHopperConfigurationPreprocessorTest {
    public static class WhenSettingDefaultAuthor {

        AtomHopperConfigurationPreprocessor atomHopperConfigurationPreprocessor;
        Configuration configuration;
        ConfigurationDefaults configurationDefaults;
        Author author;
        String authorName;
        WorkspaceConfiguration workspaceConfiguration;
        FeedConfiguration feedConfiguration;

        @Before
        public void setUp() throws Exception {
            configuration = new Configuration();
            configurationDefaults = new ConfigurationDefaults();
            author = new Author();
            authorName = "Testing Author";
            author.setName(authorName);
            configurationDefaults.setAuthor(author);
            configuration.setDefaults(configurationDefaults);
            workspaceConfiguration = new WorkspaceConfiguration();
            feedConfiguration = new FeedConfiguration();
            workspaceConfiguration.getFeed().add(feedConfiguration);
            configuration.getWorkspace().add(workspaceConfiguration);
            atomHopperConfigurationPreprocessor = new AtomHopperConfigurationPreprocessor(configuration);
        }

        @Test
        public void shouldSetAuthor() throws Exception {
            atomHopperConfigurationPreprocessor.applyDefaults();
            String authorFromFeed = configuration.getWorkspace().get(0).getFeed().get(0).getAuthor().getName();
            assertEquals("Feed should contain an author", authorName, authorFromFeed);
        }

        @Test
        public void shouldReturnConfiguration() throws Exception {
            assertTrue("This should return a configuration object", atomHopperConfigurationPreprocessor.getConfiguration() instanceof Configuration);
        }
    }
}