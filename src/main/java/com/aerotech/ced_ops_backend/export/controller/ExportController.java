package com.aerotech.ced_ops_backend.export.controller;

import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.export.dto.request.ExportRequest;
import com.aerotech.ced_ops_backend.export.dto.response.ExportJobResponse;
import com.aerotech.ced_ops_backend.export.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/exports")
@RequiredArgsConstructor
@Tag(name = "Export Center", description = "Centralized data export APIs")
public class ExportController {

    private final ExportService exportService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Export data from any module in PDF, Excel, or CSV format")
    public ResponseEntity<ApiResponse<ExportJobResponse>> export(
            @Valid @RequestBody ExportRequest request
    ) {
        ExportJobResponse response = exportService.export(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponse.<ExportJobResponse>builder()
                        .success(true)
                        .message("Export job created successfully.")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get export history for the current user")
    public ResponseEntity<ApiResponse<PageResponse<ExportJobResponse>>> getHistory(
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ExportJobResponse>>builder()
                        .success(true)
                        .message("Export history fetched successfully.")
                        .data(exportService.getExportHistory(page, size))
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get export job status and metadata")
    public ResponseEntity<ApiResponse<ExportJobResponse>> getJob(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.<ExportJobResponse>builder()
                        .success(true)
                        .message("Export job fetched successfully.")
                        .data(exportService.getExportJob(id))
                        .build()
        );
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Download exported file")
    public ResponseEntity<Resource> download(
            @PathVariable Long id
    ) throws IOException {
        ExportJobResponse job = exportService.getExportJob(id);
        Resource resource = exportService.downloadExport(id);

        String contentType = switch (job.getFormat().toUpperCase()) {
            case "PDF" -> "application/pdf";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "CSV" -> "text/csv";
            default -> "application/octet-stream";
        };

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + job.getFileName() + "\"")
                .body(resource);
    }

}
