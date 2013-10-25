package org.atomhopper.config;

import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.atomhopper.adapter.FeedInformation;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.request.adapter.GetEntryRequest;
import org.atomhopper.adapter.request.adapter.GetFeedRequest;
import org.atomhopper.config.v1_0.Configuration;
import org.atomhopper.config.v1_0.FeedConfiguration;
import org.atomhopper.response.AdapterResponse;
import org.atomhopper.util.config.ConfigurationParser;
import org.atomhopper.util.config.jaxb.JAXBConfigurationParser;
import org.atomhopper.util.config.resource.ConfigurationResource;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@RunWith( Enclosed.class )
public class WorkspaceConfigProcessorTest {

    public static class ArchiveSettings {

        private ConfigurationParser<Configuration> configurationParser;
        private WorkspaceConfigProcessor workspaceConfigProcessor;

        private FeedSource feedSource = new NoOpFeedSource();


        @Before
        public void setUp() {

            configurationParser = new JAXBConfigurationParser<Configuration>(Configuration.class,
                                                                             org.atomhopper.config.v1_0.ObjectFactory.class);

            workspaceConfigProcessor = new WorkspaceConfigProcessor( null, null, null, null, null );

        }

        @Test
        public void archiveAndCurrent() throws Exception {

            String configLocation = "/org/atomhopper/config/WorkspaceConfigProcessorTest/archiveWithCurrent.xml";

            configurationParser.setConfigurationResource(new ResourceConfigurationResource( configLocation, WorkspaceConfigProcessorTest.class ) );
            Configuration configuration = configurationParser.read();

            FeedConfiguration configFeed = configuration.getWorkspace().get( 0 ).getFeed().get( 0 );

            workspaceConfigProcessor.checkArchiving( configFeed, feedSource );
        }

        @Test( expected = ConfigurationException.class )
        public void noArchiveAndCurrent() throws Exception {

            String configLocation = "/org/atomhopper/config/WorkspaceConfigProcessorTest/noArchiveWithCurrent.xml";

            configurationParser.setConfigurationResource(new ResourceConfigurationResource( configLocation, WorkspaceConfigProcessorTest.class ) );
            Configuration configuration = configurationParser.read();

            FeedConfiguration configFeed = configuration.getWorkspace().get( 0 ).getFeed().get( 0 );

            workspaceConfigProcessor.checkArchiving( configFeed, feedSource );

        }

        @Test( expected = ConfigurationException.class )
        public void archiveAndNoCurrent() throws Exception {

            String configLocation = "/org/atomhopper/config/WorkspaceConfigProcessorTest/archiveWithNoCurrent.xml";

            configurationParser.setConfigurationResource(new ResourceConfigurationResource( configLocation, WorkspaceConfigProcessorTest.class ) );
            Configuration configuration = configurationParser.read();

            FeedConfiguration configFeed = configuration.getWorkspace().get( 0 ).getFeed().get( 0 );

            workspaceConfigProcessor.checkArchiving( configFeed, feedSource );

        }

        @Test( expected = ConfigurationException.class )
        public void archiveAndArchive() throws Exception {

            String configLocation = "/org/atomhopper/config/WorkspaceConfigProcessorTest/archiveWithArchive.xml";

            configurationParser.setConfigurationResource(new ResourceConfigurationResource( configLocation, WorkspaceConfigProcessorTest.class ) );
            Configuration configuration = configurationParser.read();

            FeedConfiguration configFeed = configuration.getWorkspace().get( 0 ).getFeed().get( 0 );

            workspaceConfigProcessor.checkArchiving( configFeed, feedSource );
        }

        @Test
        public void noArchiveAndArchive() throws Exception {

            String configLocation = "/org/atomhopper/config/WorkspaceConfigProcessorTest/noArchiveWithArchive.xml";

            configurationParser.setConfigurationResource(new ResourceConfigurationResource( configLocation, WorkspaceConfigProcessorTest.class ) );
            Configuration configuration = configurationParser.read();

            FeedConfiguration configFeed = configuration.getWorkspace().get( 0 ).getFeed().get( 0 );

            workspaceConfigProcessor.checkArchiving( configFeed, feedSource );
        }
    }

    static class NoOpFeedSource implements FeedSource {
        @Override
        public FeedInformation getFeedInformation() { return null;  }

        @Override
        public AdapterResponse<Feed> getFeed( GetFeedRequest getFeedRequest ) { return null; }

        @Override
        public AdapterResponse<Entry> getEntry( GetEntryRequest getEntryRequest ) { return null; }

        @Override
        public void setCurrentUrl( URL urlCurrent ) { }

        @Override
        public void setArchiveUrl( URL url ) { }

        @Override
        public void setParameters( Map<String, String> params ) { }
    };


    static class ResourceConfigurationResource implements ConfigurationResource {

        InputStream inputStream;

        public ResourceConfigurationResource( String path, Class clazz ) {

            inputStream = clazz.getResourceAsStream( path );
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
    }
}
