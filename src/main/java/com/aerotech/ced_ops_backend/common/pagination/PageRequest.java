package com.aerotech.ced_ops_backend.common.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * Single, shared pagination + search request DTO.
 *
 * <p>Every module's filter DTO extends this class, adding only its domain
 * specific filter fields. Base contract: {@code page}/{@code size} drive paging,
 * {@code sortBy}/{@code sortDirection} drive sorting, and {@code keyword} is a
 * free-text search token. A request is treated as a "search" (and returns a
 * {@code PageResponse}) only when paging or search criteria are actually
 * provided, preserving the legacy full-list behaviour otherwise.
 */
@Getter
@Setter
@Schema(description = "Shared pagination and filtering request. When page/size or any search "
        + "criteria are provided the endpoint returns a PageResponse; otherwise it keeps the "
        + "legacy full-list response.")
public class PageRequest {

    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 200;

    @Schema(description = "Zero-based page number", example = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer page;

    @Schema(description = "Number of records per page (capped at 200)", example = "20", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer size;

    @Schema(description = "Sort field (whitelisted per module; unknown values fall back to the default)", example = "createdAt", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sortBy;

    @Schema(description = "Sort direction: ASC or DESC", example = "DESC", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sortDirection;

    @Schema(description = "Free-text search keyword", example = "urgent", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String keyword;

    /** True when explicit paging parameters are present. */
    public boolean isPaged() {
        return page != null || size != null;
    }

    /** True when any search criterion (paging, keyword) is present. Subclasses extend this. */
    public boolean hasSearchCriteria() {
        return isPaged() || isPresent(keyword);
    }

    public int pageOrDefault() {
        return page == null ? 0 : Math.max(page, 0);
    }

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : Math.min(Math.max(size, 1), MAX_SIZE);
    }

    public static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

}
