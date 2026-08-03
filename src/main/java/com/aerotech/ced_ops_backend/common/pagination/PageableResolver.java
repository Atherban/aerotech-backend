package com.aerotech.ced_ops_backend.common.pagination;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

/**
 * Builds a Spring {@link Pageable} from the shared {@link PageRequest}, applying
 * a per-module sort whitelist and clamping the page size. Unknown sort fields
 * fall back to the module default instead of throwing.
 */
public final class PageableResolver {

    private PageableResolver() {
    }

    public static Pageable resolve(PageRequest request, Map<String, String> sortColumns, String defaultSortBy) {
        int page = request.pageOrDefault();
        int size = request.sizeOrDefault();
        Sort sort = resolveSort(request, sortColumns, defaultSortBy);
        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }

    private static Sort resolveSort(PageRequest request, Map<String, String> sortColumns, String defaultSortBy) {
        String sortBy = request.getSortBy();
        if (sortBy == null || sortBy.isBlank() || !sortColumns.containsKey(sortBy)) {
            sortBy = defaultSortBy;
        }
        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDirection())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return Sort.by(direction, sortColumns.get(sortBy));
    }

}
