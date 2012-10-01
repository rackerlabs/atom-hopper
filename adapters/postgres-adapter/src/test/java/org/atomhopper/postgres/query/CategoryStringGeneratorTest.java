package org.atomhopper.postgres.query;


import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class CategoryStringGeneratorTest {

    final String SINGLE_CAT = "+Cat1";
    final String SINGLE_CAT_RESULT = "{cat1}";

    final String MULTI_CAT = "+Cat1+Cat2";
    final String MULTI_CAT_RESULT = "{cat1,cat2}";

    @Test
    public void shouldGenerateStringForSingleCategory() {
        String result = CategoryStringGenerator.getPostgresCategoryString(SINGLE_CAT);
        assertEquals(result, SINGLE_CAT_RESULT);
    }

    @Test
    public void shouldGenerateStringForMulitpleCategories() {
        String result = CategoryStringGenerator.getPostgresCategoryString(MULTI_CAT);
        assertEquals(result, MULTI_CAT_RESULT);
    }
}
