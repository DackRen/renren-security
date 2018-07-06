package io.renren.common.specification.filter;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.renren.common.specification.filter.FilterConstant.AND;
import static io.renren.common.specification.filter.FilterConstant.OR;

@Getter
public class LogicalFilter implements Filter{
    private List<Filter> filters;
    private String logic;

    public LogicalFilter(String logic, List<Filter> filters) {
        this.filters = filters;
        this.logic = logic;
    }

    public LogicalFilter(String logic, Filter... filters) {
        this.filters = new ArrayList<>(Arrays.asList(filters));
        this.logic = logic;
    }

    public LogicalFilter add(Filter filter){
        this.filters.add(filter);
        return this;
    }

    public static LogicalFilter and(Filter f1, Filter f2){
        return new LogicalFilter(AND, f1, f2);
    }

    public static LogicalFilter or(Filter f1, Filter f2){
        return new LogicalFilter(OR, f1, f2);
    }
}
