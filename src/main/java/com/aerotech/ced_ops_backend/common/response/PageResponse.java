package com.aerotech.ced_ops_backend.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Generic paginated response wrapper")
public class PageResponse<T> {

    @Schema(description = "List of items for the current page", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> content;

    @Schema(description = "Zero-based page index", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private int page;

    @Schema(description = "Number of items per page", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private int size;

    @Schema(description = "Total number of items across all pages", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private long totalElements;

    @Schema(description = "Total number of pages", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private int totalPages;

    @Schema(description = "Whether this is the first page", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean first;

    @Schema(description = "Whether this is the last page", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean last;

    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
