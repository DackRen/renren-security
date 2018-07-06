package io.renren.common.specification.filter;

/**
 * @author rjz
 */
public class FilterConstant {
    static final String START_SIGN = "filter";
    static final int MINIMAL_LENGTH = 2;

    public static final String AND = "and";
    public static final String OR = "or";
    /** Operators*/
    public static final String EQUAL = "eq";
    public static final String NOT_EQUAL = "ne";
    public static final String IS_NULL = "isnull";
    public static final String IS_NOT_NULL = "isnotnull";
    public static final String IS_EMPTY = "isempty";
    public static final String IS_NOT_EMPTY = "isnotempty";
    public static final String CONTAINS = "contains";
    public static final String LIKE = "like";
    public static final String NOT_CONTAINS = "doesnotcontain";
    public static final String START_WITH = "startswith";
    public static final String END_WITH = "endswith";
    public static final String GREATER_THAN = "gt";
    public static final String LESS_THAN = "lt";
    public static final String GREATER_THAN_OR_EQUAL = "gte";
    public static final String LESS_THAN_OR_EQUAL = "lte";
    public static final String IN = "in";
    public static final String NIN = "nin";
    /** delimiter for crossing table search*/
    public static final String PATH_DELIMITER = ".";
}
