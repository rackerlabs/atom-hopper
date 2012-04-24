package org.atomhopper.mongodb.query;


import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.data.mongodb.core.query.Query;

@RunWith(Enclosed.class)
public class SimpleCategoryCriteriaGeneratorTest {

    public static class WhenUsingSimpleCategoryCriteriaGenerator {

        private SimpleCategoryCriteriaGenerator simpleCategoryCriteriaGenerator;
        private List<String> inclusionTerms = new ArrayList<String>();
        private Query query = new Query();
        private final String queryResult = "{ \"categories.term\" : { \"$in\" : [ \"cat1\" , \"cat2\"]}}";

        @Test
        public void shouldEnhanceCriteriaWithListSupplied() throws Exception {
            simpleCategoryCriteriaGenerator = new SimpleCategoryCriteriaGenerator("+CAT1+CAT2", inclusionTerms);
            simpleCategoryCriteriaGenerator.enhanceCriteria(query);
            assertNotNull(query.getQueryObject());
            assertEquals("Criteria should equal what was passed in", queryResult, query.getQueryObject().toString());
        }

        @Test
        public void shouldEnhanceCriteriaWithoutListSupplied() throws Exception {
            simpleCategoryCriteriaGenerator = new SimpleCategoryCriteriaGenerator("+CAT1+CAT2");
            simpleCategoryCriteriaGenerator.enhanceCriteria(query);
            assertNotNull(query.getQueryObject());
            assertEquals("Criteria should equal what was passed in", queryResult, query.getQueryObject().toString());
        }

        @Test
        public void shouldIncludeSingleTerm() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+term", inclusionTerms);

            assertEquals("Generated inclusion term should match 'term'", inclusionTerms.get(0), "term");
        }

        @Test
        public void shouldIncludeMultipleTerms() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+term_1+term_2+term_3", inclusionTerms);

            assertEquals("Generated inclusion term should match 'term_1'", "term_1", inclusionTerms.get(0));
            assertEquals("Generated inclusion term should match 'term_2'", "term_2", inclusionTerms.get(1));
            assertEquals("Generated inclusion term should match 'term_3'", "term_3", inclusionTerms.get(2));
        }

        @Test
        public void shouldHandleEscapeOperators() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+\\+term_1+\\+term_2", inclusionTerms);

            assertEquals("Generated inclusion term should match '+term_1'", "+term_1", inclusionTerms.get(0));
            assertEquals("Generated inclusion term should match '+term_3'", "+term_2", inclusionTerms.get(1));
        }

        @Test
        public void shouldHandleOperatorButNoCriteria() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+", inclusionTerms);

            assertEquals(0, inclusionTerms.size());
        }

        @Test
        public void shouldTrim() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator(" \n\t+\\+term_1+\\+term_2\t\n\t  \r", inclusionTerms);

            assertEquals("Generated inclusion term should match '+term_1'", "+term_1", inclusionTerms.get(0));
            assertEquals("Generated inclusion term should match '+term_3'", "+term_2", inclusionTerms.get(1));
        }

        @Test
        public void shouldAcceptEmptySearchStrings() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("", inclusionTerms);

            assertTrue("Generated inclusion terms should be empty", inclusionTerms.isEmpty());
        }

        @Test
        public void shouldAcceptBlankSearchStrings() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator(" \t\n\r", inclusionTerms);

            assertTrue("Generated inclusion terms should be empty", inclusionTerms.isEmpty());
        }
    }
}

