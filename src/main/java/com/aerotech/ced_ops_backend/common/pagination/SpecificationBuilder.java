package com.aerotech.ced_ops_backend.common.pagination;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reusable, fluent {@link Specification} builder.
 *
 * <p>Supports case-insensitive keyword search across multiple entity fields,
 * exact equality on a property (including nested paths like {@code role.name}),
 * and simple range filters. Built specifications are AND-composed.
 *
 * @param <T> the entity type
 */
public class SpecificationBuilder<T> {

    private final List<Specification<T>> specs = new ArrayList<>();

    public static <T> SpecificationBuilder<T> builder() {
        return new SpecificationBuilder<>();
    }

    /** Case-insensitive LIKE across the given fields, OR-composed; skipped when keyword is blank. */
    public SpecificationBuilder<T> keyword(String keyword, String... fields) {
        if (keyword == null || keyword.isBlank() || fields.length == 0) {
            return this;
        }
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        specs.add((root, query, cb) -> {
            Predicate[] predicates = new Predicate[fields.length];
            for (int i = 0; i < fields.length; i++) {
                predicates[i] = cb.like(cb.lower(stringPath(root, fields[i])), pattern);
            }
            return cb.or(predicates);
        });
        return this;
    }

    /** Exact equality; skipped when value is null (or blank for Strings). */
    public SpecificationBuilder<T> equals(String field, Object value) {
        if (value == null || (value instanceof String s && s.isBlank())) {
            return this;
        }
        specs.add((root, query, cb) -> cb.equal(stringPath(root, field), value));
        return this;
    }

    /** Column IN values; skipped when the collection is null/empty. */
    public SpecificationBuilder<T> in(String field, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return this;
        }
        specs.add((root, query, cb) -> stringPath(root, field).in(values));
        return this;
    }

    /** Greater-than-or-equal; skipped when value is null. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SpecificationBuilder<T> greaterThanOrEqualTo(String field, Comparable value) {
        if (value == null) {
            return this;
        }
        specs.add((root, query, cb) ->
                cb.greaterThanOrEqualTo((jakarta.persistence.criteria.Expression) stringPath(root, field), value));
        return this;
    }

    /** Less-than-or-equal; skipped when value is null. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public SpecificationBuilder<T> lessThanOrEqualTo(String field, Comparable value) {
        if (value == null) {
            return this;
        }
        specs.add((root, query, cb) ->
                cb.lessThanOrEqualTo((jakarta.persistence.criteria.Expression) stringPath(root, field), value));
        return this;
    }

    public Specification<T> build() {
        if (specs.isEmpty()) {
            return Specification.where(null);
        }
        Specification<T> result = Specification.where(null);
        for (Specification<T> spec : specs) {
            result = result.and(spec);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Expression<String> stringPath(Root<?> root, String field) {
        Path<?> path = root;
        for (String part : field.split("\\.")) {
            path = path.get(part);
        }
        return (Expression<String>) path;
    }

}
