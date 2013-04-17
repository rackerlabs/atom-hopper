package org.atomhopper.jdbc.query;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public class SqlBuilder {
    private String searchString;
    private SearchType type;

    private static final String EQUALS = "=";
    private static final String LESS_THAN = "<";
    private static final String GREATER_THAN = ">";

    private static final String QUESTION_MARK = "?";

    private static final String OPEN_PARENS = "(";
    private static final String CLOSE_PARENS = ")";

    private static final String SELECT_COUNT_FROM_ENTRIES = "SELECT COUNT(*) FROM entries WHERE feed = ?";

    private static final String SELECT = "SELECT * FROM entries WHERE feed = ?";
    private static final String AND = "AND";
    private static final String SPACE = " ";
    private static final String DATELASTUPDATED = "datelastupdated %s ?";
    private static final String ID = "id %s ?";

    private static final String UNION_ALL = "UNION ALL";

    private static final String ORDER_BY_ASC = "ORDER BY datelastupdated ASC, id ASC LIMIT ?";
    private static final String ORDER_BY_DESC = "ORDER BY datelastupdated DESC, id DESC LIMIT %s";

    public SqlBuilder searchString(String searchString) {
        this.searchString = searchString;
        return this;
    }

    public SqlBuilder searchType(SearchType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {

        String searchSql = SearchToSqlConverter.getSqlFromSearchString(searchString);
        StringBuilder builder = new StringBuilder();

        switch (type) {
            case FEED_FORWARD:
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, EQUALS));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(ID, GREATER_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(CLOSE_PARENS);
                builder.append(SPACE + UNION_ALL + SPACE);
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, GREATER_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(String.format(ORDER_BY_ASC));
                builder.append(CLOSE_PARENS + SPACE);
                builder.append(String.format(ORDER_BY_ASC));

                return builder.toString();

            case FEED_BACKWARD:
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, EQUALS));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(ID, LESS_THAN+EQUALS));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(CLOSE_PARENS);
                builder.append(SPACE + UNION_ALL + SPACE);
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, LESS_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(String.format(ORDER_BY_DESC, QUESTION_MARK));
                builder.append(CLOSE_PARENS + SPACE);
                builder.append(String.format(ORDER_BY_DESC, QUESTION_MARK));

                return builder.toString();

            case FEED_COUNT:
                builder.append(String.format(SELECT_COUNT_FROM_ENTRIES));

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(SPACE + AND);
                    builder.append(searchSql);
                }

                return builder.toString();

            case FEED_HEAD:
                builder.append(String.format(SELECT));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(String.format(ORDER_BY_DESC, QUESTION_MARK));

                return builder.toString();

            case NEXT_LINK:
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, EQUALS));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(ID, LESS_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(CLOSE_PARENS);
                builder.append(SPACE + UNION_ALL + SPACE);
                builder.append(OPEN_PARENS);
                builder.append(String.format(SELECT));
                builder.append(SPACE + AND + SPACE);
                builder.append(String.format(DATELASTUPDATED, LESS_THAN));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(String.format(ORDER_BY_DESC, 1));
                builder.append(CLOSE_PARENS + SPACE);
                builder.append(String.format(ORDER_BY_DESC, 1));

                return builder.toString();

            case LAST_PAGE:
            default:
                builder.append(String.format(SELECT));
                builder.append(SPACE);

                if (StringUtils.isNotBlank(searchSql)) {
                    builder.append(AND);
                    builder.append(searchSql);
                }

                builder.append(String.format(ORDER_BY_ASC));

                return builder.toString();
        }
    }

}
