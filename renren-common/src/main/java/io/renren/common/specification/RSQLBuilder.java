package io.renren.common.specification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;

@Component
public class RSQLBuilder {
    private static EntityManager em;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        em = entityManager;
    }

    public static <D> Builder<D, D> build(Class<D> domainClass) {
        return build(domainClass, domainClass);
    }

    public static <D, R> Builder<D, R> build(Class<D> domainClass, Class<R> returnClass) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<R> cq = cb.createQuery(returnClass);
        Root<D> root = cq.from(domainClass);
        return new Builder<>(cb, cq, root);
    }

    public static class Builder<D, R> {
        private CriteriaBuilder cb;
        private CriteriaQuery<R> cq;
        private Root<D> root;

        Builder(CriteriaBuilder cb, CriteriaQuery<R> cq, Root<D> root) {
            this.cb = cb;
            this.cq = cq;
            this.root = root;
        }

//        public Builder<D, R> addCreateQuery(CriteriaQuery<R> cq) {
//            this.cq = cq;
//            return this;
//        }

        public Builder<D, R> query(RQuery<D, R> rQuery) {
            this.cq = rQuery.query(root, cq, cb);
            return this;
        }

        public Builder<D, R> select(String... selects){
            this.cq.multiselect(Arrays.stream(selects).map(select -> this.root.get(select)).toArray(Path[]::new));
            return this;
        }
        public Builder<D, R> from(Class<D> domainClass){
            this.root = this.cq.from(domainClass);
            return this;
        }

        @SafeVarargs
        public final Builder<D, R> where(RSQLSpecification<D>... specifications){
            Predicate[] predicates = Arrays.stream(specifications)
                .map((specification) -> specification.toPredicate(this.root, this.cq, this.cb))
                .filter(Objects::nonNull)
                .toArray(Predicate[]::new);

            this.cq.where(predicates);
            return this;
        }

        public final Builder<D, R> where(String field, String operator, String value){
            return where(new RSQLSpecification<>(field, operator, value));
        }

        public Builder<D, R> groupBy(List<Expression<?>> group){
            this.cq.groupBy(group);
            return this;
        }

        public Builder<D, R> join(Map<String, JoinType> columnAndTypes, boolean isFetch){
            if (isFetch) {
                columnAndTypes.forEach(root::fetch);
            } else {
                columnAndTypes.forEach(this.root::join);
            }
            return this;
        }

        public CriteriaQuery<R> toCriteriaQuery() {
            return this.cq;
        }

        public TypedQuery<R> toQuery() {
            return em.createQuery(this.cq);
        }

        public Long count() {
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            query.where(cq.getRestriction());
            if (query.isDistinct()) {
                query.select(cb.countDistinct(root));
            } else {
                query.select(cb.count(root));
            }

            return em.createQuery(query).getSingleResult();
        }

        public R findOne() {
            return em.createQuery(this.cq).setMaxResults(1).getSingleResult();
        }

        public Optional<R> findFirst() {
            return em.createQuery(this.cq).setMaxResults(1).getResultList().stream().findFirst();
        }

        public List<R> findAll() {
            return em.createQuery(this.cq).getResultList();
        }

        public Page<R> findAll(Pageable page) {
            return page.isUnpaged() ? new PageImpl<>(this.findAll()) : this.readPage(page);
        }

        private Page<R> readPage(Pageable pageable) {
            TypedQuery<R> query = em.createQuery(this.cq);
            if (pageable.isPaged()) {
                query.setFirstResult((int)pageable.getOffset());
                query.setMaxResults(pageable.getPageSize());
            }

            return PageableExecutionUtils.getPage(query.getResultList(), pageable, () -> executeCountQuery(getCountQuery()));
        }

        private static Long executeCountQuery(TypedQuery<Long> query) {
            Assert.notNull(query, "TypedQuery must not be null!");
            List<Long> totals = query.getResultList();
            Long total = 0L;

            Long element;
            for(Iterator var3 = totals.iterator(); var3.hasNext(); total = total + (element == null ? 0L : element)) {
                element = (Long)var3.next();
            }

            return total;
        }

        private TypedQuery<Long> getCountQuery() {
            CriteriaQuery<Long> query = cb.createQuery(Long.class);
            if (query.isDistinct()) {
                query.select(cb.countDistinct(root));
            } else {
                query.select(cb.count(root));
            }

            query.orderBy(Collections.emptyList());
            return em.createQuery(query);
        }
    }
}
