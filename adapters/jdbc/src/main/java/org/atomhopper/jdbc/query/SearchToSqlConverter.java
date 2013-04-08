package org.atomhopper.jdbc.query;

import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;
import org.apache.commons.lang.StringUtils;

public class SearchToSqlConverter {

    public static final String OPEN_PARENS = "(";
    public static final String CLOSED_PARENS = ")";
    public static final String PLUS_SIGN = "+";
    public static final String AND = " AND ";
    public static final String OR = " OR ";
    public static final String NOT = " NOT ";

    public static final String CATEGORY = "cat";
    public static final String CATEGORY_STRING = " categories @> '{%s}'::varchar[] ";

    public static final String OLD_CATEGORY_STRING = " categories && '%s'::varchar[] ";


    public static String getSqlFromSearchString(String searchString) {

        if (StringUtils.isBlank(searchString)) {
            return null;
        }

        searchString = textToLDapSearch(searchString);

        if (searchString.startsWith(PLUS_SIGN)) {
            return getSqlForClassicSearchFormat(searchString);
        } else if (searchString.startsWith(OPEN_PARENS)) {
            Filter filter;
            try {
                filter = Filter.create(searchString);
            } catch (LDAPException ex) {
                throw new IllegalArgumentException("Invalid Search Parameter");
            }
            return getSqlFromLdapFilter(filter);
        } else {
            throw new IllegalArgumentException("Invalid Search Parameter");
        }
    }

    private static String textToLDapSearch(String searchString) {
        searchString = searchString.replace("(AND", "(&");
        searchString = searchString.replace("(OR", "(|");
        searchString = searchString.replace("(NOT", "(!");
        return searchString;
    }

    private static String getSqlForClassicSearchFormat(String searchString) {
        return String.format(OLD_CATEGORY_STRING, CategoryStringGenerator.getPostgresCategoryString(
                searchString));
    }

    private static String getSqlFromLdapFilter(Filter filter) {

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
                    throw new IllegalArgumentException("Invalid Search Parameter");
                }
                sql.append(String.format(CATEGORY_STRING, filter.getAssertionValue().toLowerCase()));
                break;
        }

        return sql.toString();
    }
}
