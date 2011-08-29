package org.atomhopper.hibernate.query;

import java.util.LinkedList;
import java.util.List;
import org.atomhopper.adapter.jpa.PersistedCategory;
import org.hibernate.Criteria;
import org.junit.Before;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class SimpleCategoryCriteriaGeneratorTest {

    public static class WhenGeneratingSearchQueries {

        private List<String> inclusionTerms, exclusionTerms;
        private Session sessionMock;
        private Criteria criteriaMock;

        @Before
        public void standUp() {
            inclusionTerms = new LinkedList<String>();
            exclusionTerms = new LinkedList<String>();
            
            criteriaMock = mock(Criteria.class);
            sessionMock = mock(Session.class);

            when(sessionMock.createCriteria(PersistedCategory.class)).thenReturn(criteriaMock);
        }

        @Test
        public void shouldExcludeSingleTerm() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("-term", inclusionTerms, exclusionTerms);

            assertEquals("Generated inclusion term should match 'term'", exclusionTerms.get(0), "term");
        }

        @Test
        public void shouldExcludeMultipleTerms() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("-term_1-term_2-term_3", inclusionTerms, exclusionTerms);

            assertEquals("Generated exclusion term should match 'term_1'", "term_1", exclusionTerms.get(0));
            assertEquals("Generated exclusion term should match 'term_2'", "term_2", exclusionTerms.get(1));
            assertEquals("Generated exclusion term should match 'term_3'", "term_3", exclusionTerms.get(2));
        }

        @Test
        public void shouldIncludeSingleTerm() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+term", inclusionTerms, exclusionTerms);

            assertEquals("Generated inclusion term should match 'term'", inclusionTerms.get(0), "term");
        }

        @Test
        public void shouldIncludeMultipleTerms() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+term_1+term_2+term_3", inclusionTerms, exclusionTerms);

            assertEquals("Generated inclusion term should match 'term_1'", "term_1", inclusionTerms.get(0));
            assertEquals("Generated inclusion term should match 'term_2'", "term_2", inclusionTerms.get(1));
            assertEquals("Generated inclusion term should match 'term_3'", "term_3", inclusionTerms.get(2));
        }

        @Test
        public void shouldHandleMultipleOperators() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+term_1-term_2+term_3-term_4", inclusionTerms, exclusionTerms);

            assertEquals("Generated inclusion term should match 'term_1'", "term_1", inclusionTerms.get(0));
            assertEquals("Generated inclusion term should match 'term_3'", "term_3", inclusionTerms.get(1));
            
            assertEquals("Generated exclusion term should match 'term_2'", "term_2", exclusionTerms.get(0));
            assertEquals("Generated exclusion term should match 'term_4'", "term_4", exclusionTerms.get(1));
        }

        @Test
        public void shouldHandleEscapeOperators() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+\\+term_1-\\-term_2+\\+term_3-\\\\term_4", inclusionTerms, exclusionTerms);

            assertEquals("Generated inclusion term should match '+term_1'", "+term_1", inclusionTerms.get(0));
            assertEquals("Generated inclusion term should match '+term_3'", "+term_3", inclusionTerms.get(1));
            assertEquals("Generated exclusion term should match '-term_2'", "-term_2", exclusionTerms.get(0));
            assertEquals("Generated exclusion term should match '\\term_4'", "\\term_4", exclusionTerms.get(1));
        }

        @Test
        public void shouldTrim() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator(" \n\t+\\+term_1-\\-term_2+\\+term_3-\\\\term_4\t\n\t  \r", inclusionTerms, exclusionTerms);

            assertEquals("Generated inclusion term should match '+term_1'", "+term_1", inclusionTerms.get(0));
            assertEquals("Generated inclusion term should match '+term_3'", "+term_3", inclusionTerms.get(1));
            assertEquals("Generated exclusion term should match '-term_2'", "-term_2", exclusionTerms.get(0));
            assertEquals("Generated exclusion term should match '\\term_4'", "\\term_4", exclusionTerms.get(1));
        }
        
        @Test
        public void shouldAcceptEmptySearchStrings() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("", inclusionTerms, exclusionTerms);

            assertTrue("Generated inclusion terms should be empty", inclusionTerms.isEmpty());
            assertTrue("Generated exclusion terms should be empty", exclusionTerms.isEmpty());
        }
        
        @Test
        public void shouldAcceptBlankSearchStrings() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator(" \t\n\r", inclusionTerms, exclusionTerms);

            assertTrue("Generated inclusion terms should be empty", inclusionTerms.isEmpty());
            assertTrue("Generated exclusion terms should be empty", exclusionTerms.isEmpty());
        }
        
        @Test
        public void shouldAddCriteria() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+term_a-term_b");
            final Criteria secondCriteriaMock = mock(Criteria.class);
            
            when(criteriaMock.createCriteria(anyString())).thenReturn(secondCriteriaMock);
            generator.enhanceCriteria(criteriaMock);
        }
    }
}
