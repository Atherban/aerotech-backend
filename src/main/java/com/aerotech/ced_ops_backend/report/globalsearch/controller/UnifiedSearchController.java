package com.aerotech.ced_ops_backend.report.globalsearch.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.request.UnifiedSearchRequest;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.response.UnifiedSearchResultItem;
import com.aerotech.ced_ops_backend.report.globalsearch.service.UnifiedSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/api/search"})
@Tag(name = "Unified Search", description = "Unified enterprise search across reports, users, and parameters")
public class UnifiedSearchController {

    private final UnifiedSearchService unifiedSearchService;

    @GetMapping(produces = {"application/json"})
    @PreAuthorize(value = "isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Unified search across reports, users and parameters",
            description = "Searches reports, users and parameters with optional entity-type, report, employee, role, shift, line, status and date filters. Returns paginated lightweight results.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request - invalid filter, date format or pagination parameter", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized - authentication required", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PageResponse<UnifiedSearchResultItem>>> search(
            @org.springdoc.core.annotations.ParameterObject UnifiedSearchRequest request) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<UnifiedSearchResultItem>>builder()
                .success(true)
                .message("Search completed successfully.")
                .data(this.unifiedSearchService.search(request))
                .build());
    }

    public UnifiedSearchController(UnifiedSearchService unifiedSearchService) {
        this.unifiedSearchService = unifiedSearchService;
    }

}
