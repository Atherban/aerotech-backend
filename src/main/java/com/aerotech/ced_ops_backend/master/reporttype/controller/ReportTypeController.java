/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.aerotech.ced_ops_backend.master.reporttype.controller;

import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.master.reporttype.dto.ReportTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/report-types"})
@Tag(name="Report Type Catalog", description="Fixed, predefined report types")
public class ReportTypeController {
    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="List all predefined report types (read-only catalog)", description="Fetches the fixed, predefined list of report types that can be created in the system.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Report types fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<ReportTypeResponse>>> getAll() {
        List<ReportTypeResponse> types = List.of(this.catalog(ReportType.PROCESS_MONITORING, "Process Monitoring"), this.catalog(ReportType.CHEMICAL_CONSUMPTION, "Chemical Consumption"), this.catalog(ReportType.DAILY_STARTUP, "Daily Startup Checklist"), this.catalog(ReportType.DAILY_INSPECTION, "Daily Inspection"), this.catalog(ReportType.FIRST_PIECE_INSPECTION, "First Piece Inspection"), this.catalog(ReportType.PDI, "Pre Delivery Inspection"));
        return ResponseEntity.ok(ApiResponse.<List<ReportTypeResponse>>builder().success(true).message("Report types fetched successfully.").data(types).build());
    }

    private ReportTypeResponse catalog(ReportType type, String name) {
        return ReportTypeResponse.builder().code(type.name()).name(name).build();
    }

    @Generated
    public ReportTypeController() {
    }
}
