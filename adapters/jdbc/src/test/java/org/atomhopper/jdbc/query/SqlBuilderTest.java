package org.atomhopper.jdbc.query;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class SqlBuilderTest {

    public static class WhenCallingSearchSql {

        private String searchString = "(cat=D)";
        private String classicSearchString = "+A+B";

        private String result_forward = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id > ? ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? ORDER BY datelastupdated ASC, id ASC LIMIT ?) ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_backward = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id <= ? ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? ORDER BY datelastupdated DESC, id DESC LIMIT ?) ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_count = "SELECT COUNT(*) FROM entries WHERE feed = ?";
        private String result_head = "SELECT * FROM entries WHERE feed = ? ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_last = "SELECT * FROM entries WHERE feed = ? ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_next = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id < ? ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? ORDER BY datelastupdated DESC, id DESC LIMIT 1) ORDER BY datelastupdated DESC, id DESC LIMIT 1";

        private String result_forward_with_cats = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id > ? AND categories @> ?::varchar[] ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated > ? AND categories @> ?::varchar[] ORDER BY datelastupdated ASC, id ASC LIMIT ?) ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_backward_with_cats = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id <= ? AND categories @> ?::varchar[] ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? AND categories @> ?::varchar[] ORDER BY datelastupdated DESC, id DESC LIMIT ?) ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_count_with_cats = "SELECT COUNT(*) FROM entries WHERE feed = ? AND categories @> ?::varchar[] ";
        private String result_head_with_cats = "SELECT * FROM entries WHERE feed = ? AND categories @> ?::varchar[] ORDER BY datelastupdated DESC, id DESC LIMIT ?";
        private String result_last_with_cats = "SELECT * FROM entries WHERE feed = ? AND categories @> ?::varchar[] ORDER BY datelastupdated ASC, id ASC LIMIT ?";
        private String result_next_with_cats = "(SELECT * FROM entries WHERE feed = ? AND datelastupdated = ? AND id < ? AND categories @> ?::varchar[] ) UNION ALL (SELECT * FROM entries WHERE feed = ? AND datelastupdated < ? AND categories @> ?::varchar[] ORDER BY datelastupdated DESC, id DESC LIMIT 1) ORDER BY datelastupdated DESC, id DESC LIMIT 1";

        private String result_classic = "SELECT COUNT(*) FROM entries WHERE feed = ? AND categories && ?::varchar[] ";
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
        public void ShouldGetSqlForCount() throws Exception {
            String result = new SqlBuilder()
                    .searchType(SearchType.FEED_COUNT)
                    .toString();

            Assert.assertEquals(result_count, result);
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
        public void ShouldGetSqlForCountWithCats() throws Exception {
            String result = new SqlBuilder()
                    .searchString(searchString)
                    .searchType(SearchType.FEED_COUNT)
                    .toString();

            Assert.assertEquals(result_count_with_cats, result);
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

        @Test
        public void ShouldGetClassicSearch() throws Exception {
            String result = new SqlBuilder()
                    .searchString(classicSearchString)
                    .searchType(SearchType.FEED_COUNT)
                    .toString();

            System.out.println(result);

            Assert.assertEquals(result_classic, result);
        }
    }
}
