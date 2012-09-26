package org.atomhopper.postgres.adapter;

import java.util.ArrayList;
import java.util.List;

public class CategoryStringGenerator {

    private static final char INCLUSIVE_OPERATOR = '+', ESCAPE_OPERATOR = '\\';
    private static final char[] OPERATORS = {INCLUSIVE_OPERATOR, ESCAPE_OPERATOR};

    public static String getPostgresCategoryString(String searchString) {

        if (searchString == null || !(searchString.trim().length() > 0)) {
            return "{}";
        }

        List<String> categories = parse(searchString.trim().toLowerCase());

        return PostgreSQLTextArray.stringArrayToPostgreSQLTextArray(categories.toArray(new String[categories.size()]));
    }

    private static List<String> parse(String searchString) {
        List<String> categories = new ArrayList<String>();

        for (int charIndex = 0; charIndex < searchString.length(); charIndex++) {
            final char nextOperator = searchString.charAt(charIndex);
            final StringBuilder searchTermBuilder = new StringBuilder();

            charIndex = readTerm(searchString, searchTermBuilder, charIndex + 1);

            switch (nextOperator) {
                case INCLUSIVE_OPERATOR:
                    categories.add(searchTermBuilder.toString());
                    break;
            }
        }

        return categories;
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
