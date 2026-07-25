package com.aerotech.ced_ops_backend.export.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing export job details")
public class ExportJobResponse {

    @Schema(description = "Export job ID", example = "1")
    private Long id;

    @Schema(description = "Export source type", example = "quality")
    private String source;

    @Schema(description = "Export format", example = "PDF")
    private String format;

    @Schema(description = "Applied filters (JSON)")
    private String filters;

    @Schema(description = "Job status", example = "COMPLETED")
    private String status;

    @Schema(description = "Generated file name", example = "export_2025-06-15_123456.pdf")
    private String fileName;

    @Schema(description = "File size in bytes", example = "204800")
    private Long fileSize;

    @Schema(description = "Job creation timestamp", example = "2025-06-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Job completion timestamp", example = "2025-06-15T10:31:00")
    private LocalDateTime completedAt;

    @Schema(description = "Error message if the job failed")
    private String errorMessage;

    @Schema(description = "Whether the exported file is available for download", example = "true")
    private boolean downloadable;

}
