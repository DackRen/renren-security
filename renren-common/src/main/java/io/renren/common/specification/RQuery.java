package io.renren.common.specification;

import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public interface RQuery<D, R> {
    @Nullable
    CriteriaQuery<R> query(Root<D> var1, CriteriaQuery<R> var2, CriteriaBuilder var3);
}
