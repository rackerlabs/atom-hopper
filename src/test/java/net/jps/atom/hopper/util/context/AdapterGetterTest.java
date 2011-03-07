/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.jps.atom.hopper.util.context;

import java.util.Calendar;
import net.jps.atom.hopper.adapter.AdapterTools;
import net.jps.atom.hopper.response.AdapterResponse;
import net.jps.atom.hopper.response.EmptyBody;
import org.apache.abdera.model.Entry;
import org.apache.abdera.model.Feed;
import org.apache.abdera.protocol.server.RequestContext;
import org.junit.Before;
import com.rackspace.cloud.commons.util.servlet.context.ApplicationContextAdapter;
import net.jps.atom.hopper.adapter.FeedSourceAdapter;
import net.jps.atom.hopper.adapter.archive.FeedArchiveAdapter;
import net.jps.atom.hopper.adapter.impl.UnimplementedFeedArchive;
import net.jps.atom.hopper.adapter.impl.UnimplementedFeedSource;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author zinic
 */
@RunWith(Enclosed.class)
public class AdapterGetterTest {

    public static final String BAD_REFERENCE = "bean-reference-bad",
            NULL_REFERENCE = "null-reference",
            FEED_SOURCE_REFERENCE = "feed-source",
            FEED_ARCHIVE_REFERENCE = "feed-archive";

    @Ignore
    public static class InstanceableClass {
    }

    @Ignore
    public static abstract class NonInstanceableClass implements FeedSourceAdapter {
    }

    @Ignore
    public static class TestParent {

        protected AdapterGetter adapterGetter;
        protected ApplicationContextAdapter contextAdapterMock;

        @Before
        public void standUp() {
            contextAdapterMock = mock(ApplicationContextAdapter.class);

            when(contextAdapterMock.fromContext(eq(BAD_REFERENCE), any(Class.class))).thenReturn(new InstanceableClass());
            when(contextAdapterMock.fromContext(eq(NULL_REFERENCE), any(Class.class))).thenReturn(null);

            when(contextAdapterMock.fromContext(eq(UnimplementedFeedSource.class))).thenReturn(new UnimplementedFeedSource());
            when(contextAdapterMock.fromContext(eq(UnimplementedFeedArchive.class))).thenReturn(new UnimplementedFeedArchive());

            when(contextAdapterMock.fromContext(eq(FEED_SOURCE_REFERENCE), any(Class.class))).thenReturn(new UnimplementedFeedSource());
            when(contextAdapterMock.fromContext(eq(FEED_ARCHIVE_REFERENCE), any(Class.class))).thenReturn(new UnimplementedFeedArchive());

            adapterGetter = new AdapterGetter(contextAdapterMock);
        }
    }

    public static class WhenGettingFromContext extends TestParent {

        @Test
        public void shouldGetFromContext() {
            assertNotNull(adapterGetter.getFeedSource(UnimplementedFeedSource.class));
            assertNotNull(adapterGetter.getFeedArchive(UnimplementedFeedArchive.class));
            assertNotNull(adapterGetter.getFeedSource(FEED_SOURCE_REFERENCE));
            assertNotNull(adapterGetter.getFeedArchive(FEED_ARCHIVE_REFERENCE));
        }

        @Test(expected = AdapterNotFoundException.class)
        public void shouldThrowExceptionWhenReferenceReturnsNull() {
            adapterGetter.getFeedSource(NULL_REFERENCE);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldRejectBlankAdapterBeanReferenceNames() {
            adapterGetter.getFeedSource("");
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldRejectNullAdapterBeanReferenceNames() {
            final String ref = null;

            adapterGetter.getFeedSource(ref);
        }

        @Test(expected = AdapterConstructionException.class)
        public void shouldFailWhenGivenNonInstanceableClasses() {
            assertNull(adapterGetter.getFeedSource(NonInstanceableClass.class));
        }
    }

    public static class WhenCheckingForTypeSafety extends TestParent {

        @Test(expected = ClassCastException.class)
        public void shouldDetectClassCastingErrors() {
            assertNull(adapterGetter.getFeedArchive(BAD_REFERENCE));
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldCheckForArchiveInterface() {
            adapterGetter.getFeedArchive(InstanceableClass.class);
        }
        
        @Test(expected = IllegalArgumentException.class)
        public void shouldCheckForFeedSourceInterface() {
            adapterGetter.getFeedSource(InstanceableClass.class);
        }
    }
}
