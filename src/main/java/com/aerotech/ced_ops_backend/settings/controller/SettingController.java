package com.aerotech.ced_ops_backend.settings.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.settings.dto.request.BulkUpdateSettingsRequest;
import com.aerotech.ced_ops_backend.settings.dto.request.CreateSettingRequest;
import com.aerotech.ced_ops_backend.settings.dto.request.UpdateSettingRequest;
import com.aerotech.ced_ops_backend.settings.dto.response.SystemSettingResponse;
import com.aerotech.ced_ops_backend.settings.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "System Settings", description = "System settings management APIs")
public class SettingController {

    private final SettingService settingService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settings fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    @Operation(summary = "Get all settings with optional search and pagination")
    public ResponseEntity<ApiResponse<PageResponse<SystemSettingResponse>>> search(
            @RequestParam(required = false) @Parameter(description = "Search keyword in setting key") String keyword,
            @RequestParam(required = false) @Parameter(description = "Filter by category") String category,
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<SystemSettingResponse>>builder()
                        .success(true)
                        .message("Settings fetched successfully.")
                        .data(settingService.search(keyword, category, page, size))
                        .build()
        );
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All active settings fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    @Operation(summary = "Get all active settings as a flat list")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getAll() {
        return ResponseEntity.ok(
                ApiResponse.<List<SystemSettingResponse>>builder()
                        .success(true)
                        .message("Settings fetched successfully.")
                        .data(settingService.getAll())
                        .build()
        );
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Setting fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Setting not found")
    })
    @Operation(summary = "Get a single setting by key")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> getByKey(
            @PathVariable @Parameter(description = "Setting key") String key
    ) {
        return ResponseEntity.ok(
                ApiResponse.<SystemSettingResponse>builder()
                        .success(true)
                        .message("Setting fetched successfully.")
                        .data(settingService.getByKey(key))
                        .build()
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Setting created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Setting key already exists")
    })
    @Operation(summary = "Create a new system setting")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> create(
            @Valid @RequestBody CreateSettingRequest request
    ) {
        SystemSettingResponse response = settingService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{key}")
                .buildAndExpand(response.getSettingKey())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<SystemSettingResponse>builder()
                        .success(true)
                        .message("Setting created successfully.")
                        .data(response)
                        .build()
        );
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Setting updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Setting not found")
    })
    @Operation(summary = "Update an existing system setting")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> update(
            @PathVariable @Parameter(description = "Setting key") String key,
            @Valid @RequestBody UpdateSettingRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<SystemSettingResponse>builder()
                        .success(true)
                        .message("Setting updated successfully.")
                        .data(settingService.update(key, request))
                        .build()
        );
    }

    @PutMapping("/category/{category}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Settings updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    })
    @Operation(summary = "Bulk update settings for a specific category")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> bulkUpdateByCategory(
            @PathVariable @Parameter(description = "Setting category") String category,
            @Valid @RequestBody BulkUpdateSettingsRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<SystemSettingResponse>>builder()
                        .success(true)
                        .message("Settings updated successfully.")
                        .data(settingService.bulkUpdateByCategory(category, request))
                        .build()
        );
    }

    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Setting deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires SUPER_ADMIN role"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Setting not found")
    })
    @Operation(summary = "Delete a system setting")
    public ResponseEntity<Void> delete(
            @PathVariable @Parameter(description = "Setting key") String key
    ) {
        settingService.delete(key);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN or SUPER_ADMIN role")
    })
    @Operation(summary = "Get all setting categories")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(
                ApiResponse.<List<String>>builder()
                        .success(true)
                        .message("Categories fetched successfully.")
                        .data(settingService.getCategories())
                        .build()
        );
    }

}
