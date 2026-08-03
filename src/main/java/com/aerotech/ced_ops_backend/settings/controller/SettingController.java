/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.parameters.RequestBody
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.Min
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.servlet.support.ServletUriComponentsBuilder
 */
package com.aerotech.ced_ops_backend.settings.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.settings.dto.request.BulkUpdateSettingsRequest;
import com.aerotech.ced_ops_backend.settings.dto.request.CreateSettingRequest;
import com.aerotech.ced_ops_backend.settings.dto.request.UpdateSettingRequest;
import com.aerotech.ced_ops_backend.settings.dto.response.SystemSettingResponse;
import com.aerotech.ced_ops_backend.settings.service.SettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import lombok.Generated;
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

@RestController
@RequestMapping(value={"/api/settings"})
@Tag(name="System Settings", description="System settings management APIs")
public class SettingController {
    private final SettingService settingService;

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get all settings with optional search and pagination", description="Retrieves system settings with optional keyword search on the setting key and filtering by category, returned in a paginated envelope. Accessible to users with the SUPER_ADMIN or ADMIN role.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Settings fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PageResponse<SystemSettingResponse>>> search(@RequestParam(required=false) @Parameter(description="Search keyword in setting key", example="maintenance") String keyword, @RequestParam(required=false) @Parameter(description="Filter by category", example="GENERAL") String category, @RequestParam(defaultValue="0") @Min(value=0L) @Parameter(description="Page number (zero-based)", example="0") @Min(value=0L) int page, @RequestParam(defaultValue="20") @Min(value=1L) @Parameter(description="Page size", example="20") @Min(value=1L) int size) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<SystemSettingResponse>>builder().success(true).message("Settings fetched successfully.").data(this.settingService.search(keyword, category, page, size)).build());
    }

    @GetMapping(value={"/all"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get all active settings as a flat list", description="Retrieves all active system settings as a flat list without pagination. Accessible to users with the SUPER_ADMIN or ADMIN role.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="All active settings fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.<List<SystemSettingResponse>>builder().success(true).message("Settings fetched successfully.").data(this.settingService.getAll()).build());
    }

    @GetMapping(value={"/{key}"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get a single setting by key", description="Retrieves a single system setting by its unique key. Accessible to users with the SUPER_ADMIN or ADMIN role.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Setting fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Setting not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<SystemSettingResponse>> getByKey(@Parameter(description="Setting key", example="app.maintenance_mode", required=true) @PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.<SystemSettingResponse>builder().success(true).message("Setting fetched successfully.").data(this.settingService.getByKey(key)).build());
    }

    @PostMapping(produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Create a new system setting", description="Creates a new system setting. Only accessible to users with the SUPER_ADMIN role.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="System setting creation payload", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=CreateSettingRequest.class), examples={@ExampleObject(name="CreateSettingRequestExample", summary="Example create setting request", value="{\"settingKey\": \"app.maintenance_mode\", \"settingValue\": \"false\", \"category\": \"GENERAL\", \"dataType\": \"BOOLEAN\", \"description\": \"Enable or disable maintenance mode\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Setting created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="409", description="Conflict - setting key already exists", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<SystemSettingResponse>> create(@Valid @RequestBody CreateSettingRequest request) {
        SystemSettingResponse response = this.settingService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{key}").buildAndExpand(new Object[]{response.getSettingKey()}).toUri();
        return ResponseEntity.created((URI)location).body(ApiResponse.<SystemSettingResponse>builder().success(true).message("Setting created successfully.").data(response).build());
    }

    @PutMapping(value={"/{key}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Update an existing system setting", description="Updates the value, data type, description and active status of an existing system setting. Only accessible to users with the SUPER_ADMIN role.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="System setting update payload", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=UpdateSettingRequest.class), examples={@ExampleObject(name="UpdateSettingRequestExample", summary="Example update setting request", value="{\"settingValue\": \"true\", \"dataType\": \"BOOLEAN\", \"description\": \"Enable or disable maintenance mode\", \"isActive\": true}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Setting updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Setting not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<SystemSettingResponse>> update(@Parameter(description="Setting key", example="app.maintenance_mode", required=true) @PathVariable String key, @Valid @RequestBody UpdateSettingRequest request) {
        return ResponseEntity.ok(ApiResponse.<SystemSettingResponse>builder().success(true).message("Setting updated successfully.").data(this.settingService.update(key, request)).build());
    }

    @PutMapping(value={"/category/{category}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Bulk update settings for a specific category", description="Bulk updates multiple system settings belonging to a specific category in a single request. Only accessible to users with the SUPER_ADMIN role.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Bulk update payload containing the list of setting key-value pairs", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=BulkUpdateSettingsRequest.class), examples={@ExampleObject(name="BulkUpdateSettingsRequestExample", summary="Example bulk update settings request", value="{\"settings\": [{\"settingKey\": \"app.maintenance_mode\", \"settingValue\": \"true\"}, {\"settingKey\": \"app.session_timeout_minutes\", \"settingValue\": \"30\"}]}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Settings updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Category not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> bulkUpdateByCategory(@Parameter(description="Setting category", example="GENERAL", required=true) @PathVariable String category, @Valid @RequestBody BulkUpdateSettingsRequest request) {
        return ResponseEntity.ok(ApiResponse.<List<SystemSettingResponse>>builder().success(true).message("Settings updated successfully.").data(this.settingService.bulkUpdateByCategory(category, request)).build());
    }

    @DeleteMapping(value={"/{key}"}, produces={"application/json"})
    @PreAuthorize(value="hasRole('SUPER_ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete a system setting", description="Permanently deletes a system setting by its unique key. Only accessible to users with the SUPER_ADMIN role.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Setting deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Setting not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="Setting key", example="app.maintenance_mode", required=true) @PathVariable String key) {
        this.settingService.delete(key);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value={"/categories"}, produces={"application/json"})
    @PreAuthorize(value="hasAnyRole('SUPER_ADMIN','ADMIN')")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get all setting categories", description="Retrieves the list of all available setting categories. Accessible to users with the SUPER_ADMIN or ADMIN role.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Categories fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="403", description="Forbidden - requires ADMIN or SUPER_ADMIN role", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.<List<String>>builder().success(true).message("Categories fetched successfully.").data(this.settingService.getCategories()).build());
    }

    @Generated
    public SettingController(SettingService settingService) {
        this.settingService = settingService;
    }
}
