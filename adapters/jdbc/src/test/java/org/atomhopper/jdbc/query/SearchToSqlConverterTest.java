package org.atomhopper.jdbc.query;

import static junit.framework.Assert.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        private String classic_search_result = "( categories @> ?::varchar[] )";

        private String no_plus_or_parens_search = "search";
        private String bad_ldap_search = "(cat=D";

        private String incorrect_category_term = ("search=A)");

        private String prefix_single_search= "(cat=tid:D)";
        private String prefix_single_search_result = " tenantId = ? ";

        private String prefix_and_search = "(&(cat=tid:A)(cat=B))";
        private String prefix_and_search_result = "( tenantId = ?  AND  categories @> ?::varchar[] )";

        private String prefix_or_search = "(|(cat=tid:A)(cat=B))";
        private String prefix_or_search_result = "( tenantId = ?  OR  categories @> ?::varchar[] )";

        private String prefix_not_search = "(!(cat=tid:A))";
        private String prefix_not_search_result = " NOT  tenantId = ? ";

        private String prefix_classic_search = "+tid:A+B";
        private String prefix_classic_search_result = "( tenantId = ?  OR  categories @> ?::varchar[] )";

        private static Map<String, String> prefixMapper = new HashMap<String, String>();

        static {
            prefixMapper.put( "tid", "tenantId" );
            prefixMapper.put( "type", "eventtype" );
        }

        private static String PREFIX_SPLIT = ":";



        @Test
        public void ShouldGetCats() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            List<String> result = searchToSqlConverter.getParamsFromSearchString(single_search);
            assertTrue(result.size() == 1);
        }

        @Test
        public void ShouldGetCatsPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_single_search);
            assertTrue(result.size() == 1);
            assertEquals( result.get( 0 ), "D" );
        }

        @Test
        public void ShouldGetCatsForAnd() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            List<String> result = searchToSqlConverter.getParamsFromSearchString(and_search);
            assertTrue(result.size() == 2);
        }

        @Test
        public void ShouldGetCatsForAndPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_and_search);
            assertTrue(result.size() == 2);
            assertEquals( result.get( 0 ), "A" );
            assertEquals( result.get( 1 ), "{b}" );
        }

        @Test
        public void ShouldGetCatsForOr() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            List<String> result = searchToSqlConverter.getParamsFromSearchString(or_search);
            assertTrue(result.size() == 2);
        }

        @Test
        public void ShouldGetCatsForOrPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_or_search);
            assertTrue(result.size() == 2);
            assertEquals( result.get( 0 ), "A" );
            assertEquals( result.get( 1 ), "{b}" );
        }

        @Test
        public void ShouldGetCatsForNot() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            List<String> result = searchToSqlConverter.getParamsFromSearchString(not_search);
            assertTrue(result.size() == 1);
        }

        @Test
        public void ShouldGetCatsForNotPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_not_search);
            assertTrue(result.size() == 1);
            assertEquals( result.get( 0 ), "A" );
        }

        @Test
        public void ShouldGetSqlForSingle() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(single_search);
            assertEquals(single_search_result, result);
        }

        @Test
        public void ShouldGetSqlForSinglePrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            String result = searchToSqlConverter.getSqlFromSearchString(prefix_single_search);
            assertEquals(prefix_single_search_result, result);
        }

        @Test
        public void ShouldGetSqlForAnd() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(and_search);
            assertEquals(and_search_result, result);
        }

        @Test
        public void ShouldGetSqlForAndPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            String result = searchToSqlConverter.getSqlFromSearchString(prefix_and_search);
            assertEquals(prefix_and_search_result, result);
        }

        @Test
        public void ShouldGetSqlForOr() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(or_search);
            assertEquals(or_search_result, result);
        }

        @Test
        public void ShouldGetSqlForOrPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            String result = searchToSqlConverter.getSqlFromSearchString(prefix_or_search);
            assertEquals(prefix_or_search_result, result);
        }

        @Test
        public void ShouldGetSqlForNot() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(not_search);
            assertEquals(not_search_result, result);
        }

        @Test
        public void ShouldGetSqlForNotPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            String result = searchToSqlConverter.getSqlFromSearchString(prefix_not_search);
            assertEquals(prefix_not_search_result, result);
        }

        @Test
        public void ShouldGetSqlForAndCurl() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(and_curl_search);
            assertEquals(and_search_result, result);
        }

        @Test
        public void ShouldGetSqlForOrCurl() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(or_curl_search);
            assertEquals(or_search_result, result);
        }

        @Test
        public void ShouldGetSqlForNotCurl() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(not_curl_search);
            assertEquals(not_search_result, result);
        }

        @Test
        public void ShouldGetSqlForClassic() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(classic_search);
            assertEquals(classic_search_result, result);
        }

        @Test
        public void ShouldGetSqlForClassicPrefix() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            String result = searchToSqlConverter.getSqlFromSearchString(prefix_classic_search);
            assertEquals(prefix_classic_search_result, result);
        }

        @Test(expected = IllegalArgumentException.class)
        public void ShouldErrorForInvalidSearchNoOpeningPlusOrParens() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(no_plus_or_parens_search);
        }

        @Test(expected = IllegalArgumentException.class)
        public void ShouldErrorForInvalidSearchBadLdap() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(bad_ldap_search);
        }

        @Test(expected = IllegalArgumentException.class)
        public void ShouldErrorForIncorrectCategoryTerm() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(incorrect_category_term);
        }

        @Test
        public void ShouldReturnNullForBlank() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter();

            String result = searchToSqlConverter.getSqlFromSearchString(null);
            assertNull(result);
        }


        @Test
        public void ShouldGetPrefixCats() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_single_search);
            assertTrue(result.size() == 1);
        }

        @Test
        public void ShouldGetPrefixCatsForAnd() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_and_search);
            assertTrue(result.size() == 2);
        }

        @Test
        public void ShouldGetPrefixCatsForOr() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_or_search);
            assertTrue(result.size() == 2);
        }

        @Test
        public void ShouldGetPrefixCatsForNot() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_not_search);
            assertTrue(result.size() == 1);
        }

        @Test
        public void ShouldGetPrefixCatsClassic() throws Exception {
            SearchToSqlConverter searchToSqlConverter = new SearchToSqlConverter( prefixMapper, PREFIX_SPLIT );

            List<String> result = searchToSqlConverter.getParamsFromSearchString(prefix_classic_search);
            assertTrue(result.size() == 2);
            assertEquals( result.get( 0 ), "a" );
            assertEquals( result.get( 1 ), "{b}" );
        }
    }
}
