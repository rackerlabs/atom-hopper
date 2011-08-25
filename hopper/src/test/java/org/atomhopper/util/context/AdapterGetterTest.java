package org.atomhopper.util.context;

import org.atomhopper.util.context.AdapterNotFoundException;
import org.atomhopper.util.context.AdapterConstructionException;
import org.atomhopper.util.context.AdapterGetter;
import org.atomhopper.adapter.FeedSource;
import org.atomhopper.adapter.impl.UnimplementedFeedSource;
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

    public static final String BAD_REFERENCE = "bean-reference-bad",
            NULL_REFERENCE = "null-reference",
            FEED_SOURCE_REFERENCE = "feed-source",
            FEED_ARCHIVE_REFERENCE = "feed-archive";

    @Ignore
    public static class InstanceableClass {
    }

    @Ignore
    public static abstract class NonInstanceableClass implements FeedSource {
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
            when(contextAdapterMock.fromContext(eq(FEED_SOURCE_REFERENCE), any(Class.class))).thenReturn(new UnimplementedFeedSource());

            adapterGetter = new AdapterGetter(contextAdapterMock);
        }
    }

    public static class WhenGettingFromContext extends TestParent {

        @Test
        public void shouldGetFromContext() {
            assertNotNull(adapterGetter.getByClassDefinition(UnimplementedFeedSource.class, FeedSource.class));
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
