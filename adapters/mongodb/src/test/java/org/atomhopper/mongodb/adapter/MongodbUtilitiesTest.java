package org.atomhopper.mongodb.adapter;

import static junit.framework.Assert.assertEquals;
import static org.atomhopper.mongodb.adapter.MongodbUtilities.formatCollectionName;
import static org.atomhopper.mongodb.adapter.MongodbUtilities.safeLongToInt;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;


@RunWith(Enclosed.class)
public class MongodbUtilitiesTest {

    public static class WhenCallingMongodbUtilities {

        private final String SHORT_FORMATTED_COLLECTION_NAME = "namespace.feed";
        private final String SHORT_COLLECTION_NAME = "namespace/feed";

        private final String LONG_FORMATTED_COLLECTION_NAME = "namespace.feed.1234567890.1234567890.1234567890.1234567890.1234567890.";
        private final String LONG_COLLECTION_NAME = "namespace/feed/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890/1234567890";

        @Test
        public void shouldformatShortCollectionName() throws Exception {
            assertEquals("Should return formatted collection name (short)", SHORT_FORMATTED_COLLECTION_NAME, formatCollectionName(SHORT_COLLECTION_NAME));
        }

        @Test
        public void shouldformatLongCollectionName() throws Exception {
            assertEquals("Should return formatted collection name (long)", LONG_FORMATTED_COLLECTION_NAME, formatCollectionName(LONG_COLLECTION_NAME));
        }

        @Test(expected=IllegalArgumentException.class)
        public void shouldThrowErrorWhenConvertingWithMaxValuePlusOne() throws Exception {
            long maxValueForTest = (long)Integer.MAX_VALUE + (long)1;
            safeLongToInt(maxValueForTest);
        }

        @Test(expected=IllegalArgumentException.class)
        public void shouldThrowErrorWhenConvertingWithMinValueMinusOne() throws Exception {
            long minValueForTest = (long)Integer.MIN_VALUE - (long)1;
            safeLongToInt(minValueForTest);
        }

        @Test
        public void shouldReturnConvertedValue() throws Exception {
            assertEquals("Should return value", 1, safeLongToInt(1));
        }
    }
}