package org.atomhopper.jdbc.query;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(Enclosed.class)
public class SearchToSqlConverterTest {

    public static class WhenCallingSearchToSqlConverter {

        private String single_search = "(cat=D)";
        private String single_search_result = " categories @> ?::varchar[] ";

        private String and_search = "(&(cat=A)(cat=B))";
        private String and_curl_search = "(AND(cat=A)(cat=B))";
        private String and_search_result = "( categories @> ?::varchar[]  AND  categories @> ?::varchar[] )";

        private String or_search = "(|(cat=A)(cat=B))";
        private String or_curl_search = "(OR(cat=A)(cat=B))";
        private String or_search_result = "( categories @> ?::varchar[]  OR  categories @> ?::varchar[] )";

        private String not_search = "(!(cat=A))";
        private String not_curl_search = "(NOT(cat=A))";
        private String not_search_result = " NOT  categories @> ?::varchar[] ";

        private String classic_search = "+A";
        private String classic_search_result = " categories && ?::varchar[] ";

        private String no_plus_or_parens_search = "search";
        private String bad_ldap_search = "(cat=D";

        private String incorrect_category_term = ("search=A)");


        @Test
        public void ShouldGetCats() throws Exception {
            List<String> result = SearchToSqlConverter.getParamsFromSearchString(single_search);
            Assert.assertTrue(result.size() == 1);
        }

        @Test
        public void ShouldGetCatsForAnd() throws Exception {
            List<String> result = SearchToSqlConverter.getParamsFromSearchString(and_search);
            Assert.assertTrue(result.size() == 2);
        }

        @Test
        public void ShouldGetCatsForOr() throws Exception {
            List<String> result = SearchToSqlConverter.getParamsFromSearchString(or_search);
            Assert.assertTrue(result.size() == 2);
        }

        @Test
        public void ShouldGetCatsForNot() throws Exception {
            List<String> result = SearchToSqlConverter.getParamsFromSearchString(not_search);
            Assert.assertTrue(result.size() == 1);
        }

        @Test
        public void ShouldGetSqlForSingle() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(single_search);
            Assert.assertEquals(single_search_result, result);
        }

        @Test
        public void ShouldGetSqlForAnd() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(and_search);
            Assert.assertEquals(and_search_result, result);
        }

        @Test
        public void ShouldGetSqlForOr() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(or_search);
            Assert.assertEquals(or_search_result, result);
        }

        @Test
        public void ShouldGetSqlForNot() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(not_search);
            Assert.assertEquals(not_search_result, result);
        }

        @Test
        public void ShouldGetSqlForAndCurl() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(and_curl_search);
            Assert.assertEquals(and_search_result, result);
        }

        @Test
        public void ShouldGetSqlForOrCurl() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(or_curl_search);
            Assert.assertEquals(or_search_result, result);
        }

        @Test
        public void ShouldGetSqlForNotCurl() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(not_curl_search);
            Assert.assertEquals(not_search_result, result);
        }

        @Test
        public void ShouldGetSqlForClassic() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(classic_search);
            Assert.assertEquals(classic_search_result, result);
        }

        @Test(expected = IllegalArgumentException.class)
        public void ShouldErrorForInvalidSearchNoOpeningPlusOrParens() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(no_plus_or_parens_search);
        }

        @Test(expected = IllegalArgumentException.class)
        public void ShouldErrorForInvalidSearchBadLdap() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(bad_ldap_search);
        }

        @Test(expected = IllegalArgumentException.class)
        public void ShouldErrorForIncorrectCategoryTerm() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(incorrect_category_term);
        }

        @Test
        public void ShouldReturnNullForBlank() throws Exception {
            String result = SearchToSqlConverter.getSqlFromSearchString(null);
            Assert.assertNull(result);
        }
    }
}
