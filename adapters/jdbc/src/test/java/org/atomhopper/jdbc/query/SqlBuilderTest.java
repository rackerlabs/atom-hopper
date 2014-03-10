package org.atomhopper.jdbc.query;

import junit.framework.Assert;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class SqlBuilderTest {

    public static class WhenCallingSearchSql {

        private String searchString = "(cat=D)";

        private String result_forward = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id > ? ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? ORDER BY datelastupdated ASC, id ASC LIMIT ?) ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_backward = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id <= ? ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? ORDER BY datelastupdated DESC, id DESC LIMIT ?) ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_head = "SELECT * FROM entries WHERE feed = ? ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_last = "SELECT * FROM entries WHERE feed = ? ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_next = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id < ? ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? ORDER BY datelastupdated DESC, id DESC LIMIT 1) ORDER BY datelastupdated DESC, id DESC LIMIT 1";

        private String result_forward_with_cats = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id > ? AND categories @> ?::varchar[] ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? AND categories @> ?::varchar[] ORDER BY datelastupdated ASC, id ASC LIMIT ?) ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_backward_with_cats = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id <= ? AND categories @> ?::varchar[] ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? AND categories @> ?::varchar[] ORDER BY datelastupdated DESC, id DESC LIMIT ?) ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_head_with_cats = "SELECT * FROM entries WHERE feed = ? AND categories @> ?::varchar[] ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_last_with_cats = "SELECT * FROM entries WHERE feed = ? AND categories @> ?::varchar[] ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_next_with_cats = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id < ? AND categories @> ?::varchar[] ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? AND categories @> ?::varchar[] ORDER BY datelastupdated DESC, id DESC LIMIT 1) ORDER BY datelastupdated DESC, id DESC LIMIT 1";

        private DateTimeFormatter isoDTF = ISODateTimeFormat.dateTimeNoMillis();

        @Test
        public void ShouldGetSqlForForwad() throws Exception {
            String result = new SqlBuilder()
                    .searchType(SearchType.FEED_FORWARD)
                    .toString();

            Assert.assertEquals(result_forward, result);
        }

        @Test
        public void ShouldGetSqlForBackward() throws Exception {
            String result = new SqlBuilder()
                    .searchType(SearchType.FEED_BACKWARD)
                    .toString();

            Assert.assertEquals(result_backward, result);
        }

        @Test
        public void ShouldGetSqlForHead() throws Exception {
            String result = new SqlBuilder()
                    .searchType(SearchType.FEED_HEAD)
                    .toString();

            Assert.assertEquals(result_head, result);
        }

        @Test
        public void ShouldGetSqlForLast() throws Exception {
            String result = new SqlBuilder()
                    .searchType(SearchType.LAST_PAGE)
                    .toString();

            Assert.assertEquals(result_last, result);
        }

        @Test
        public void ShouldGetSqlForNext() throws Exception {
            String result = new SqlBuilder()
                    .searchType(SearchType.NEXT_LINK)
                    .toString();

            Assert.assertEquals(result_next, result);
        }

        @Test
        public void ShouldGetSqlForForwadWithCats() throws Exception {
            String result = new SqlBuilder()
                    .searchString(searchString)
                    .searchType(SearchType.FEED_FORWARD)
                    .toString();

            Assert.assertEquals(result_forward_with_cats, result);
        }

        @Test
        public void ShouldGetSqlForBackwardWithCats() throws Exception {
            String result = new SqlBuilder()
                    .searchString(searchString)
                    .searchType(SearchType.FEED_BACKWARD)
                    .toString();

            Assert.assertEquals(result_backward_with_cats, result);
        }

        @Test
        public void ShouldGetSqlForHeadWithCats() throws Exception {
            String result = new SqlBuilder()
                    .searchString(searchString)
                    .searchType(SearchType.FEED_HEAD)
                    .toString();

            Assert.assertEquals(result_head_with_cats, result);
        }

        @Test
        public void ShouldGetSqlForLastWithCats() throws Exception {
            String result = new SqlBuilder()
                    .searchString(searchString)
                    .searchType(SearchType.LAST_PAGE)
                    .toString();

            Assert.assertEquals(result_last_with_cats, result);
        }

        @Test
        public void ShouldGetSqlForNextWithCats() throws Exception {
            String result = new SqlBuilder()
                    .searchString(searchString)
                    .searchType(SearchType.NEXT_LINK)
                    .toString();

            Assert.assertEquals(result_next_with_cats, result);
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldGetExceptionOnStartingAtMissingTimezone() throws Exception {
            DateTime startAt = isoDTF.parseDateTime("2014-03-03T08:51:32");
            String result = new SqlBuilder()
                                    .searchType(SearchType.BY_TIMESTAMP_BACKWARD)
                                    .startingTimestamp(startAt)
                                    .toString();
        }

        @Test(expected = IllegalArgumentException.class)
        public void shouldGetExceptionOnStartingAtInvalidFormat() throws Exception {
            DateTime startAt = isoDTF.parseDateTime("20140303T08:51:32Z");
            String result = new SqlBuilder()
                                    .searchType(SearchType.BY_TIMESTAMP_BACKWARD)
                                    .startingTimestamp(startAt)
                                    .toString();
        }

        @Test
        public void shouldGetSelectWithTimestamp() throws Exception {
            String startingTimestamp =  "2014-03-03T08:51:32Z";
            DateTime startAt = isoDTF.parseDateTime(startingTimestamp);
            String result = new SqlBuilder()
                                    .searchType(SearchType.BY_TIMESTAMP_BACKWARD)
                                    .startingTimestamp(startAt)
                                    .toString();

            String timestamp = startAt.getHourOfDay() + ":" + startAt.getMinuteOfHour() + ":" + startAt.getSecondOfMinute();
            Assert.assertTrue("result string has timezone", result.contains("current_setting('TIMEZONE')"));
            Assert.assertTrue("result string has correct time", result.contains(timestamp));
        }
    }
}
