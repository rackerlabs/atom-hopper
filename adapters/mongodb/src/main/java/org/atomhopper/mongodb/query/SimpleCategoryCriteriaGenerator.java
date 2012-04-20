package org.atomhopper.mongodb.query;

import java.util.LinkedList;
import java.util.List;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public class SimpleCategoryCriteriaGenerator implements CategoryCriteriaGenerator {

    private static final char INCLUSIVE_OPERATOR = '+', ESCAPE_OPERATOR = '\\';
    private static final char[] OPERATORS = {INCLUSIVE_OPERATOR, ESCAPE_OPERATOR};
    private final List<String> inclusionTerms;
    private boolean hasTerms;

    public SimpleCategoryCriteriaGenerator(String searchString) {
        this(searchString, new LinkedList<String>());
    }

    SimpleCategoryCriteriaGenerator(String searchString, List<String> inclusionTerms) {
        this.inclusionTerms = inclusionTerms;

        this.hasTerms = false;

        if (searchString.trim().length() <= 1) {
            return;
        }

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
            }
        }
    }

    @Override
    public void enhanceCriteria(Query ongoingQuery) {
        if (hasTerms) {
            if (!inclusionTerms.isEmpty()) {
                // ongoingQuery.addCriteria(Criteria.where("term").in(inclusionTerms));
                ongoingQuery.addCriteria(Criteria.where("categories.term").in(inclusionTerms));
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
