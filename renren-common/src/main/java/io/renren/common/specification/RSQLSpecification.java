/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.renren.common.specification;

import io.renren.common.specification.exception.SpecificationException;
import io.renren.common.specification.filter.ComparisonFilter;
import io.renren.common.specification.filter.Filter;
import io.renren.common.specification.filter.LogicalFilter;
import lombok.Data;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static io.renren.common.specification.filter.FilterConstant.*;

/**
 * @author renjunzhou
 */
@Data
public class RSQLSpecification<T> implements Specification<T>{
    private Filter filter;
    private Class<T> classz;

    public RSQLSpecification() {
        this.filter = new LogicalFilter(AND, new ArrayList<>());
    }

    public RSQLSpecification(String field, String op, Object value) {
        this.filter = new ComparisonFilter(field, op, value);
    }

    public RSQLSpecification(Filter filter) {
        this.filter = filter;
    }

    public RSQLSpecification<T> and(String field, String op, Object value) {
        and(new ComparisonFilter(field, op, value));
        return this;
    }

    public RSQLSpecification<T> or(String field, String op, Object value) {
        or(new ComparisonFilter(field, op, value));
        return this;
    }

    public RSQLSpecification<T> and(@Nullable ComparisonFilter other) {
        if (other != null) {
            this.filter = LogicalFilter.and(this.filter, other);
        }
        return this;
    }

    public RSQLSpecification<T> and(Filter ...filters) {
        for(Filter other: filters){
            this.filter = LogicalFilter.and(this.filter, other);
        }
        return this;
    }

    public RSQLSpecification<T> or(@Nullable ComparisonFilter other) {
        if (other != null) {
            this.filter = LogicalFilter.or(this.filter, other);
        }
        return this;
    }

    @Nullable
    @Override
    public Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> cq, @NonNull CriteriaBuilder cb) throws SpecificationException {
        return getPredicate(this.filter, root, cb);
    }

    private Predicate getPredicate(Filter filter, Path<T> root, CriteriaBuilder cb) throws SpecificationException {
        if(this.filter == null){
            return null;
        }

        //single filter
        if (filter instanceof ComparisonFilter) {
            return getPredicateByOperator(root, cb, (ComparisonFilter)filter);
        }

        //logic filters
        List<Predicate> predicateList = new LinkedList<>();
        for (Filter f : ((LogicalFilter) filter).getFilters()) {
            Predicate predicate = getPredicate(f, root, cb);
            if (predicate != null) {
                predicateList.add(predicate);
            }
        }
        Predicate[] predicates = predicateList.toArray(new Predicate[0]);

        if(predicates.length > 0){
            switch (((LogicalFilter) filter).getLogic()) {
                case AND:
                    return cb.and(predicates);
                case OR:
                    return cb.or(predicates);
                default:
                    throw new SpecificationException("Unknown filter logic" + ((LogicalFilter) filter).getLogic());
            }
        }
        return null;
    }

    /**
     * implement operator
     * @param root extend from extends path
     * @param cb CriteriaBuilder
     * @return single Predicate
     */
    private <Y> Predicate getPredicateByOperator(Path<T> root, CriteriaBuilder cb, ComparisonFilter filter){
        Path<Y> path = parsePath(root, filter.getField());
        Y value = null;
        Y[] values = null;

        if(path == null){
            return null;
        }

        if (filter.getOperator().equals(IN) || filter.getOperator().equals(NIN)) {
            values = parseArrayValue(path, filter.getValue());
        } else if (filter.getOperator().equals(IS_NULL) || filter.getOperator().equals(IS_NOT_NULL)){
        } else {
            value = parseValue(path, (Y) filter.getValue());
        }

        switch(filter.getOperator()){
            case EQUAL:
                return cb.equal(path, value);
            case NOT_EQUAL:
                return cb.notEqual(path, value);
            case IS_NULL:
                return cb.isNull(path);
            case IS_NOT_NULL:
                return cb.isNotNull(path);
            case GREATER_THAN:
                if (value instanceof LocalDateTime) {
                    return cb.greaterThan(path.as(LocalDateTime.class), (LocalDateTime) value);
                }
                if(value instanceof Double){
                    return cb.greaterThan(path.as(Double.class), (Double) value);
                }
                return cb.greaterThan(path.as(String.class), value.toString());
            case GREATER_THAN_OR_EQUAL:
                if (value instanceof LocalDateTime) {
                    return cb.greaterThanOrEqualTo(path.as(LocalDateTime.class), (LocalDateTime) value);
                }
                if(value instanceof Double){
                    return cb.greaterThanOrEqualTo(path.as(Double.class), (Double) value);
                }
                return cb.greaterThanOrEqualTo(path.as(String.class), value.toString());
            case LESS_THAN:
                value = parseValue(path, value);
                if (value instanceof LocalDateTime) {
                    return cb.lessThan(path.as(LocalDateTime.class), (LocalDateTime) value);
                }
                if(value instanceof Double){
                    return cb.lessThan(path.as(Double.class), (Double) value);
                }
                return cb.lessThan(path.as(String.class), value.toString());
            case LESS_THAN_OR_EQUAL:
                value = parseValue(path, value);
                if (value instanceof LocalDateTime) {
                    return cb.lessThanOrEqualTo(path.as(LocalDateTime.class), (LocalDateTime) value);
                }
                if(value instanceof Double){
                    return cb.lessThanOrEqualTo(path.as(Double.class), (Double) value);
                }
                return cb.lessThanOrEqualTo(path.as(String.class), value.toString());
            case IN:
                return path.in(values);
            case NIN:
                return path.in(values).not();
            /*
                Operator for String type
             */
            case IS_EMPTY:
                return cb.equal(path,"");
            case IS_NOT_EMPTY:
                return cb.notEqual(path,"");
            case LIKE:
            case CONTAINS:
                return cb.like(cb.upper(path.as(String.class)), "%" + String.valueOf(value).toUpperCase() + "%");
            case NOT_CONTAINS:
                return cb.notLike(cb.upper(path.as(String.class)), "%" + String.valueOf(value).toUpperCase() + "%");
            case START_WITH:
                return cb.like(cb.upper(path.as(String.class)), String.valueOf(value).toUpperCase() + "%");
            case END_WITH:
                return cb.like(cb.upper(path.as(String.class)), "%" + String.valueOf(value).toUpperCase());
            default:
                throw new IllegalStateException("unknown operator: " + filter.getOperator());
        }
    }

    private <Y> Path<Y> parsePath(Path<?> root, String field) {
        try {
            if (!field.contains(PATH_DELIMITER)) {
                return root.get(field);
            }
            int i = field.indexOf(PATH_DELIMITER);
            String firstPart = field.substring(0, i);
            String secondPart = field.substring(i + 1, field.length());
            Path<?> p = root.get(firstPart);
            return parsePath(p, secondPart);
        }catch(IllegalArgumentException e){
            return null;
        }
    }

    private <Y> Y[] parseArrayValue(Path<Y> path, Object value) {
        Class<?> type = path.getJavaType();

        // parse Enum
        if (Enum.class.isAssignableFrom(type) && value instanceof String[]) {
            List<Enum> enums = new ArrayList<>();
            for (String s : (String[]) value) {
                enums.add(Enum.valueOf((Class<? extends Enum>) type, s));
            }
            return (Y[]) enums.toArray();
        }
        if(value instanceof Object[]){
            return (Y[]) value;
        }
        if(value instanceof Collection){
            return (Y[]) ((Collection) value).toArray();
        }
        return (Y[]) new Object[]{value};
    }

    /**
     * parse value to the suitable type by path
     * @param path extends Expression
     * @param value is the value that is needed to parse
     * @return value with correct type
     */
    private <Y> Y parseValue(Path<Y> path, Y value) {
        Class<? extends Y> type = path.getJavaType();

        // parse Date
        if (Date.class.isAssignableFrom(type) || LocalDateTime.class.isAssignableFrom(type)) {
            return (Y) LocalDateTime.parse(value.toString(), DateTimeFormatter.ISO_DATE_TIME);
        }

        //
        if (YearMonth.class.isAssignableFrom(type)) {
            return (Y) YearMonth.parse(value.toString());
        }

        // parse Enum
        if (Enum.class.isAssignableFrom(type)) {
            return (Y) Enum.valueOf((Class<? extends Enum>) type, value.toString());
        }

        // parse Boolean
        if (boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
            return (Y) Boolean.valueOf(value.toString());
        }

        return value;
    }
}
