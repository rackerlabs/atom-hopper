package org.atomhopper.util.context;

import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.impl.DisabledFeedSource;
import org.atomhopper.servlet.ApplicationContextAdapter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 *
 * 
 */
@RunWith(Enclosed.class)
public class AdapterGetterTest {

    private static final String BAD_REFERENCE = "bean-reference-bad";
    private static final String NULL_REFERENCE = "null-reference";
    private static final String FEED_SOURCE_REFERENCE = "feed-source";
    public static final String FEED_ARCHIVE_REFERENCE = "feed-archive";

    @Ignore
    private static class InstanceableClass {
    }

    @Ignore
    private static abstract class NonInstanceableClass implements FeedSource {
    }

    @Ignore
    public static class TestParent {

        AdapterGetter adapterGetter;
        ApplicationContextAdapter contextAdapterMock;

        @Before
        public void standUp() {
            contextAdapterMock = mock(ApplicationContextAdapter.class);

            when(contextAdapterMock.fromContext(eq(BAD_REFERENCE), any(Class.class))).thenReturn(new InstanceableClass());
            when(contextAdapterMock.fromContext(eq(NULL_REFERENCE), any(Class.class))).thenReturn(null);
            when(contextAdapterMock.fromContext(eq(DisabledFeedSource.class))).thenReturn((DisabledFeedSource.getInstance()));
            when(contextAdapterMock.fromContext(eq(FEED_SOURCE_REFERENCE), any(Class.class))).thenReturn(DisabledFeedSource.getInstance());

            adapterGetter = new AdapterGetter(contextAdapterMock);
        }
    }

    public static class WhenGettingFromContext extends TestParent {

        @Test
        public void shouldGetFromContext() {
            assertNotNull(adapterGetter.getByClassDefinition(DisabledFeedSource.class, FeedSource.class));
            assertNotNull(adapterGetter.getByName(FEED_SOURCE_REFERENCE, FeedSource.class));
        }

        @Test(expected = AdapterNotFoundException.class)
        public void shouldThrowExceptionWhenReferenceReturnsNull() {
            adapterGetter.getByName(NULL_REFERENCE, FeedSource.class);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldRejectBlankAdapterBeanReferenceNames() {
            adapterGetter.getByName("", FeedSource.class);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldRejectNullAdapterBeanReferenceNames() {
            final String ref = null;

            adapterGetter.getByName(ref, FeedSource.class);
        }

        @Test(expected = AdapterConstructionException.class)
        public void shouldFailWhenGivenNonInstanceableClasses() {
            adapterGetter.getByClassDefinition(NonInstanceableClass.class, FeedSource.class);
        }
    }

    public static class WhenCheckingForTypeSafety extends TestParent {

        @Test(expected = IllegalArgumentException.class)
        public void shouldDetectClassCastingErrors() {
            adapterGetter.getByName(BAD_REFERENCE, FeedSource.class);
        }
    }
}
