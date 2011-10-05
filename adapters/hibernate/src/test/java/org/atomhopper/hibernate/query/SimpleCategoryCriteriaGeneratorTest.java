package org.atomhopper.hibernate.query;

import java.util.LinkedList;
import java.util.List;
import org.atomhopper.adapter.jpa.PersistedCategory;
import org.hibernate.Criteria;
import org.junit.Before;
import org.hibernate.Session;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class SimpleCategoryCriteriaGeneratorTest {

    public static class WhenGeneratingSearchQueries {

        private List<String> inclusionTerms;
        private Session sessionMock;
        private Criteria criteriaMock;

        @Before
        public void standUp() {
            inclusionTerms = new LinkedList<String>();
            
            criteriaMock = mock(Criteria.class);
            sessionMock = mock(Session.class);

            when(sessionMock.createCriteria(PersistedCategory.class)).thenReturn(criteriaMock);
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
        
        @Test
        public void shouldAddCriteria() {
            final SimpleCategoryCriteriaGenerator generator = new SimpleCategoryCriteriaGenerator("+term_a");
            final Criteria secondCriteriaMock = mock(Criteria.class);
            
            when(criteriaMock.createCriteria(anyString())).thenReturn(secondCriteriaMock);
            generator.enhanceCriteria(criteriaMock);
        }
    }
}
