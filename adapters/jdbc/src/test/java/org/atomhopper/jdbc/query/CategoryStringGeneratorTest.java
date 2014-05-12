package org.atomhopper.jdbc.query;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

public class CategoryStringGeneratorTest {

    final String SINGLE_CAT = "+Cat1";
    final String SINGLE_CAT_RESULT = "{cat1}";

    final String MULTI_CAT = "+Cat1+Cat2";
    final String MULTI_CAT_RESULT = "{cat1,cat2}";

    Map<String, String> prefixMap = new HashMap<String, String>();
    final String SPLIT = ":";

    final String COL_CAT = "+pOne:col+cat1";

    @Before
    public void setUp() throws Exception {

        prefixMap.put( "pone", "cone" );
        prefixMap.put( "ptwo", "ctwo" );
    }

    @Test
    public void shouldGenerateTwoItems() {

        List<String> result = CategoryStringGenerator.getPostgresCategoryString( COL_CAT, prefixMap, SPLIT );
        assertEquals( result.size(), 2 );
        assertEquals(result.get( 0 ), "col" );
        assertEquals(result.get( 1 ), "{cat1}" );
    }

    @Test
    public void shouldGenerateStringForSingleCategory() {
        List<String> result = CategoryStringGenerator.getPostgresCategoryString(SINGLE_CAT, Collections.EMPTY_MAP, null );
        assertEquals( result.size(), 1 );
        assertEquals(result.get( 0 ), SINGLE_CAT_RESULT);
    }

    @Test
    public void shouldGenerateStringForMulitpleCategories() {
        List<String> result = CategoryStringGenerator.getPostgresCategoryString(MULTI_CAT, Collections.EMPTY_MAP, null );
        assertEquals( result.size(), 1 );
        assertEquals(result.get( 0 ), MULTI_CAT_RESULT);
    }
}
