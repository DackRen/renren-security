package io.renren.common.specification.filter;

import io.renren.common.specification.exception.SpecificationException;

import java.util.*;
import java.util.stream.Collectors;

import static io.renren.common.specification.filter.FilterConstant.*;


/**
 * @author rjz
 */
public class FilterParser {
    public static LogicalFilter parse(Map<String, String[]> params){
        List<Filter> filters = new ArrayList<>();

        params.forEach((k, v) -> {
            StringTokenizer tokenizer = new StringTokenizer(k, "[]", false);
            if(tokenizer.countTokens() >= MINIMAL_LENGTH && START_SIGN.equals(tokenizer.nextToken())){
                addFilter(tokenizer, v, filters);
            }
        });
        return new LogicalFilter(AND, filters);
    }

    /**
     * generate Filter by tokenizer and values and add it to filters of their parent node
     *
     * @param tokenizer
     * @param values
     * @param filters
     */
    private static void addFilter(StringTokenizer tokenizer, String[] values, List<Filter> filters) {
        String next = tokenizer.nextToken();
        // Only parent node has logic operator
        // Otherwise it is a leaf node with expression
        if(next.equals(OR) || next.equals(AND)){
            // Check whether exist of this logic node
            for (Filter filter : filters){
                if(filter instanceof LogicalFilter && Objects.equals(((LogicalFilter) filter).getLogic(), next)){
                    addFilter(tokenizer, values, ((LogicalFilter) filter).getFilters());
                    return;
                }
            }
            // If not then create a new logic node
            List<Filter> currentFilters = new ArrayList<>();
            Filter currentFilter = new LogicalFilter(next, currentFilters);
            filters.add(currentFilter);
            addFilter(tokenizer, values, currentFilters);
        } else {
            ComparisonFilter comparisonFilter = getComparisonFilter(next, tokenizer, values);
            if (comparisonFilter != null) {
                filters.add(comparisonFilter);
            }
        }
    }

    private static ComparisonFilter getComparisonFilter(String field, StringTokenizer tokenizer, String... values){
        String op;
        switch (tokenizer.countTokens()) {
            // filter[id]=1;
            case 0:
                op = EQUAL;
                break;
            // filter[id][contain]=1
            case 1:
                op = tokenizer.nextToken();
                break;
            default:
                throw new SpecificationException("bad param quantity : " + tokenizer.countTokens());
        }

        // ignore empty value
        boolean isNotUnaryOperator = !op.equals(IS_NULL) && !op.equals(IS_NOT_NULL) && !op.equals(IS_EMPTY) && !op.equals(IS_NOT_EMPTY);
        // TODO
        boolean notExistValue = values.length == 0 || (values.length == 1 && "".equals(values[0]));

        if(isNotUnaryOperator){
            if(notExistValue){
                return null;
            }
        }
        //TODO
        if (Arrays.asList(IN, NIN).contains(op)) {
            return new ComparisonFilter(field, op, values);
        } else {
            //TODO if op is not in type, then values need be a single String
            return new ComparisonFilter(field, op, values[0]);
        }
    }
}
