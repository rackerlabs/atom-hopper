package org.atomhopper.dynamodb.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CategoryStringGenerator {

    private static final char INCLUSIVE_OPERATOR = '+', ESCAPE_OPERATOR = '\\';
    private static final char[] OPERATORS = {INCLUSIVE_OPERATOR, ESCAPE_OPERATOR};

    private CategoryStringGenerator() {
        throw new AssertionError();
    }

    /**
     * Takes a search string, prefix map & prefix character & returns an array of strings which are arguments to the
     * corresponding generated SQL statement.
     *
     * See SearchToSqlConverter for more details.
     *
     * @param searchString
     * @param mapPrefix
     * @param prefixSplit
     *
     * @return
     */
    public static List<String> getPostgresCategoryString(String searchString, Map<String, String> mapPrefix, String prefixSplit ) {

        List<String> finalList = new ArrayList<String>();
        if (searchString == null || !(searchString.trim().length() > 0)) {
            finalList.add( "{}" );
            return finalList;
        }

        List<String> categories = parse(searchString.trim().toLowerCase());
        List<String> catHolder = new ArrayList<String>();

        // find if any categories are prefixed, if so, split them out.
        for( String cat : categories ) {
            if (cat.matches( SQLToNoSqlConverter.BAD_SEARCH_REGEX ) ) {
                throw new IllegalArgumentException( SQLToNoSqlConverter.BAD_CHAR_MSG );
            }

            if (prefixSplit != null ) {
                int index = cat.indexOf( prefixSplit );
                if ( index != -1 ) {
                    String prefix = cat.substring( 0, index );

                    if ( mapPrefix.containsKey( prefix ) ) {
                        addToFinalList( finalList, catHolder );
                        finalList.add( cat );
                    }
                    else {
                        catHolder.add( cat );
                    }
                }
                else {
                    catHolder.add( cat );
                }
            }
            else {
                catHolder.add( cat );
            }
        }

        addToFinalList( finalList, catHolder );
        return finalList;
    }

    private static void addToFinalList( List<String> finalList, List<String> catHolder ) {

        if ( !catHolder.isEmpty() ) {

            String psArray = DynamoDBTextArray.stringArrayToPostgreSQLTextArray( catHolder.toArray( new String[ catHolder
                  .size() ] ) );
            finalList.add( psArray );

            catHolder.clear();
        }
    }

    private static List<String> parse(String searchString) {
        List<String> categories = new ArrayList<String>();

        for (int charIndex = 0; charIndex < searchString.length(); charIndex++) {
            final char nextOperator = searchString.charAt(charIndex);
            final StringBuilder searchTermBuilder = new StringBuilder();

            charIndex = readTerm(searchString, searchTermBuilder, charIndex + 1);

            switch (nextOperator) {
                case INCLUSIVE_OPERATOR:
                    if( searchTermBuilder.toString().isEmpty() ) {

                        throw new IllegalArgumentException( "Invalid Search Parameter: Parameter cannot be empty string." );
                    }

                    categories.add(searchTermBuilder.toString());
                    break;

                default:
                    break;
            }
        }

        return categories;
    }

    private static int readTerm(String searchString, StringBuilder builder, int currentCharIndex) {
        int charIndex = currentCharIndex;
        boolean isEscaped = false;


        while( charIndex < searchString.length() ) {
            final char nextChar = searchString.charAt(charIndex);

            if (isEscaped || !isOperator(nextChar)) {
                builder.append( nextChar );
                isEscaped = false;
            } else {
                if (nextChar == ESCAPE_OPERATOR) {
                    isEscaped = true;
                } else {
                    return charIndex - 1;
                }
            }

            charIndex++;
        }

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
