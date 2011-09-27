package org.atomhopper.hibernate.query;

import java.util.LinkedList;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleCategoryCriteriaGenerator implements CategoryCriteriaGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleCategoryCriteriaGenerator.class);
    private static final char INCLUSIVE_OPERATOR = '+', EXCLUSIVE_OPERATOR = '-', ESCAPE_OPERATOR = '\\';
    private static final char[] OPERATORS = {INCLUSIVE_OPERATOR, EXCLUSIVE_OPERATOR, ESCAPE_OPERATOR};
    private final List<String> inclusionTerms, exclusionTerms;
    private boolean hasTerms;

    public SimpleCategoryCriteriaGenerator(String searchString) {
        this(searchString, new LinkedList<String>(), new LinkedList<String>());
    }

    SimpleCategoryCriteriaGenerator(String searchString, List<String> inclusionTerms, List<String> exclusionTerms) {
        this.inclusionTerms = inclusionTerms;
        this.exclusionTerms = exclusionTerms;
        
        this.hasTerms = false;

        parse(searchString.trim().toLowerCase());
    }

    private void parse(String searchString) {
        for (int charIndex = 0; charIndex < searchString.length(); charIndex++) {
            final char nextOperator = searchString.charAt(charIndex);
            final StringBuilder searchTermBuilder = new StringBuilder();

            charIndex = readTerm(searchString, searchTermBuilder, charIndex + 1);

            switch (nextOperator) {
                case INCLUSIVE_OPERATOR:
                    hasTerms = true;
                    inclusionTerms.add(searchTermBuilder.toString());
                    break;

                case EXCLUSIVE_OPERATOR:
                    hasTerms = true;
                    exclusionTerms.add(searchTermBuilder.toString());
                    break;
            }
        }       
    }

    @Override
    public void enhanceCriteria(Criteria ongoingCriteria) {
        if (hasTerms) {
            final Criteria newSearchCriteria = ongoingCriteria.createCriteria("categories");

            if (!inclusionTerms.isEmpty()) {
                newSearchCriteria.add(Restrictions.in("term", inclusionTerms));
            }

            if (!exclusionTerms.isEmpty()) {
                newSearchCriteria.add(Restrictions.not(Restrictions.in("term", exclusionTerms)));
            }
        }
    }

    private static int readTerm(String searchString, StringBuilder builder, int currentCharIndex) {
        int charIndex = currentCharIndex;
        boolean isEscaped = false;

        do {
            final char nextChar = searchString.charAt(charIndex);

            if (isEscaped || !isOperator(nextChar)) {
                builder.append(nextChar);
                isEscaped = false;
            } else {
                if (nextChar == ESCAPE_OPERATOR) {
                    isEscaped = true;
                } else {
                    return charIndex - 1;
                }
            }
        } while (++charIndex < searchString.length());

        return charIndex;
    }

    private static boolean isOperator(char character) {
        for (char operator : OPERATORS) {
            if (operator == character) {
                return true;
            }
        }

        return false;
    }
}
