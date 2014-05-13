package org.atomhopper.jdbc.query;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class creates the search SQL and corresponding SQL parameters for a given set of query parameters.
 *
 * By default, this class assumes all categories are treated the same and stored in a single variable-length array.
 *
 * This class can also be customized to map specific categories to specific columns in the DB, allowing for higher
 * performance when searching on these categories.
 *
 * To configure this functionality pass in the following:
 *
 * <ul>
 *     <li>Map[String, String] - a map of pairs where the key is the prefix found in the atom category and the
 *     value is the name of the SQL text column.</li>
 *     <li>String - the String which separates the prefix from the value within the atom category.  E.g.,
 *     for the category value of "tid:1234", if ':' is the mark, then "tid" is the prefix.</li>
 * </ul>
 *
 * An example configuration might be:
 *
 * A map of { "tid" => "tenantid", "type" => "eventype" } with a mark of ":".
 *
 * This maps the following atom categories:
 *
 * <ul>
 *     <li>"tid:1234" => enter "1234" into the "tenantid" column</li>
 *     <li>"type:lbaas.usage" => enter "lbaas.usage" into the "eventtype" column</li>
 * </ul>
 */
public class SearchToSqlConverter {

    private static final String OPEN_PARENS = "(";
    private static final String CLOSED_PARENS = ")";
    private static final String OPEN_CURLY_BRACKET = "{";
    private static final String CLOSED_CURLY_BRACKET = "}";
    private static final String PLUS_SIGN = "+";
    private static final String AND = " AND ";
    private static final String OR = " OR ";
    private static final String NOT = " NOT ";

    private static final String CATEGORY = "cat";
    private static final String CATEGORY_STRING = " categories @> ?::varchar[] ";

    public static final String OLD_CATEGORY_STRING = " categories && ?::varchar[] ";

    private static final String COLUMN_STRING= " = ? ";

    private String prefixSplit = null;

    private Map<String, String> mapPrefix = new HashMap<String, String>();

    public SearchToSqlConverter() { }

    public SearchToSqlConverter( Map<String, String> mapper, String split ) {

        prefixSplit = split;
        mapPrefix = new HashMap<String, String>( mapper );
    }


    public String getSqlFromSearchString(String searchString) {

        if (StringUtils.isBlank(searchString)) {
            return null;
        }

        if (searchString.startsWith(PLUS_SIGN)) {
            return getSqlForClassicSearchFormat(searchString);
        } else if (searchString.startsWith(OPEN_PARENS)) {
            searchString = textToLDapSearch(searchString);
            Filter filter;
            try {
                filter = Filter.create(searchString);
            } catch (LDAPException ex) {
                throw new IllegalArgumentException("Invalid LDAP Search Parameter");
            }
            return getSqlFromLdapFilter(filter);
        } else {
            throw new IllegalArgumentException("Invalid Search Parameter: Search must begin with a '+' or a '(' character");
        }
    }

    public List<String> getParamsFromSearchString(String searchString) {

        if (StringUtils.isBlank(searchString)) {
            return new ArrayList<String>();
        }

        if (searchString.startsWith(PLUS_SIGN)) {
            return getParametersForClassicSearchFormat((searchString));
        } else if (searchString.startsWith(OPEN_PARENS)) {
            searchString = textToLDapSearch(searchString);
            Filter filter;
            try {
                filter = Filter.create(searchString);
            } catch (LDAPException ex) {
                throw new IllegalArgumentException("Invalid LDAP Search Parameter");
            }
            return getParametersFromLdapFilter(filter);
        } else {
            throw new IllegalArgumentException("Invalid Search Parameter: Search must begin with a '+' or a '(' character");
        }
    }

    private String textToLDapSearch(String searchString) {
        searchString = searchString.replace("(AND", "(&");
        searchString = searchString.replace("(OR", "(|");
        searchString = searchString.replace("(NOT", "(!");
        return searchString;
    }

    private String getSqlForClassicSearchFormat(String searchString) {

        String[] params = searchString.split( "\\+" );

        List<String> sqlList = new ArrayList<String>();
        String last = "";

        // first item is an empty string, so we skip
        for( int i = 1; i < params.length; i++ ) {

            String state = createSql( params[ i ], OLD_CATEGORY_STRING );

            // if we have several generic categories, we only need 1 sql statement to handle them
            if( !(state.equals( OLD_CATEGORY_STRING ) && last.equals( OLD_CATEGORY_STRING ) ) ) {

                sqlList.add( state );
            }

            last = state;
        }

        StringBuilder sql = new StringBuilder();

        sql.append(OPEN_PARENS);

        for( int i = 0; i < sqlList.size(); i++ ) {

            if ( i > 0 )
                sql.append( OR );

            sql.append( sqlList.get( i ) );
        }

        sql.append(CLOSED_PARENS);

        return sql.toString();
    }

    private List<String> getParametersForClassicSearchFormat(String searchString) {
        List<String> params = new ArrayList<String>();
        params.addAll(CategoryStringGenerator.getPostgresCategoryString(searchString, mapPrefix, prefixSplit ) );
        return params;
    }

    private String getSqlFromLdapFilter(Filter filter) {

        StringBuilder sql = new StringBuilder();

        Filter[] filters = filter.getComponents();
        Filter notFilter = filter.getNOTComponent();

        switch (filter.getFilterType()) {

            case Filter.FILTER_TYPE_AND:
                for (int x=0 ; x < filters.length; x++) {
                    if (x == 0) {
                        sql.append(OPEN_PARENS);
                    }
                    if (x > 0) {
                        sql.append(AND);
                    }
                    sql.append(getSqlFromLdapFilter(filters[x]));
                    if (x == filters.length - 1) {
                        sql.append(CLOSED_PARENS);
                    }
                }
                break;

            case Filter.FILTER_TYPE_OR:
                for (int x=0 ; x < filters.length; x++) {
                    if (x == 0) {
                        sql.append(OPEN_PARENS);
                    }
                    if (x > 0) {
                        sql.append(OR);
                    }
                    sql.append(getSqlFromLdapFilter(filters[x]));
                    if (x == filters.length - 1) {
                        sql.append(CLOSED_PARENS);
                    }
                }
                break;

            case Filter.FILTER_TYPE_NOT:
                sql.append(NOT);
                sql.append(getSqlFromLdapFilter(notFilter));
                break;

            case Filter.FILTER_TYPE_EQUALITY:
                if (!filter.getAttributeName().equals(CATEGORY)) {
                    throw new IllegalArgumentException("Invalid Search Parameter: LDAP attribute name must be 'cat'");
                }

                sql.append( createSql( filter.getAssertionValue(), CATEGORY_STRING ) );

                break;
        }

        return sql.toString();
    }

    private String createSql( String param, String defaultSql ) {

        if( prefixSplit != null ) {

            int index = param.indexOf( prefixSplit );

            if ( index != -1 ) {

                String prefix = param.substring( 0, index );

                // detect prefix in map
                if ( mapPrefix.containsKey( prefix ) ) {

                    String column = mapPrefix.get( prefix );

                    return " " + column + COLUMN_STRING;
                }
            }
        }

        return defaultSql;
    }

    private List<String> getParametersFromLdapFilter(Filter filter) {

        List<String> params = new ArrayList<String>();

        Filter[] filters = filter.getComponents();
        Filter notFilter = filter.getNOTComponent();

        switch (filter.getFilterType()) {

            case Filter.FILTER_TYPE_AND:
            case Filter.FILTER_TYPE_OR:
                for (int x=0 ; x < filters.length; x++) {
                    params.addAll(getParametersFromLdapFilter(filters[x]));
                }
                break;

            case Filter.FILTER_TYPE_NOT:
                params.addAll(getParametersFromLdapFilter(notFilter));
                break;

            case Filter.FILTER_TYPE_EQUALITY:
                if (!filter.getAttributeName().equals(CATEGORY)) {
                    throw new IllegalArgumentException("Invalid Search Parameter: LDAP attribute name must be 'cat'");
                }

                // default
                String param = OPEN_CURLY_BRACKET + filter.getAssertionValue().toLowerCase() + CLOSED_CURLY_BRACKET;

                if( prefixSplit != null ) {

                    int index = filter.getAssertionValue().indexOf( prefixSplit );

                    if ( index != -1 ) {

                        String prefix = filter.getAssertionValue().substring( 0, index );
                        String value = filter.getAssertionValue().substring( index + prefixSplit.length() );

                        if ( mapPrefix.containsKey( prefix ) ) {

                            param = value;
                        }
                    }
                }

                params.add( param );
                break;
        }

        return params;
    }
}
