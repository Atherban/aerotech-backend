package com.aerotech.ced_ops_backend.report.globalsearch.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.request.GlobalSearchRequest;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.response.GlobalSearchResultItem;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.response.SearchSuggestionsResponse;
import com.aerotech.ced_ops_backend.report.globalsearch.service.GlobalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/search")
@RequiredArgsConstructor
@Tag(name = "Global Search", description = "Global report search and filtering APIs")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Operation(summary = "Global search across all report modules with filtering, pagination and sorting")
    public ResponseEntity<ApiResponse<PageResponse<GlobalSearchResultItem>>> search(
            @Parameter(hidden = true) GlobalSearchRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<GlobalSearchResultItem>>builder()
                        .success(true)
                        .message("Search completed successfully.")
                        .data(globalSearchService.search(request))
                        .build()
        );
    }

    @GetMapping("/suggestions")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Suggestions fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Operation(summary = "Get search suggestions for report numbers, employee names, lines, and processes")
    public ResponseEntity<ApiResponse<SearchSuggestionsResponse>> suggestions(
            @RequestParam @Parameter(description = "Search query text") String q
    ) {
        return ResponseEntity.ok(
                ApiResponse.<SearchSuggestionsResponse>builder()
                        .success(true)
                        .message("Suggestions fetched successfully.")
                        .data(globalSearchService.getSuggestions(q))
                        .build()
        );
    }

}
