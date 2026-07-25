package com.aerotech.ced_ops_backend.export.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Request to create an export job")
public class ExportRequest {

    @NotBlank(message = "Export source is required")
    @Size(max = 100, message = "Source must not exceed 100 characters")
    @Parameter(description = "Export source type")
    @Schema(description = "Export source type", example = "quality")
    private String source;

    @NotBlank(message = "Export format is required")
    @Size(max = 10, message = "Format must not exceed 10 characters")
    @Parameter(description = "Export format: PDF, EXCEL, CSV")
    @Schema(description = "Export format: PDF, EXCEL, CSV", example = "PDF")
    private String format;

    @Size(max = 50, message = "Report type must not exceed 50 characters")
    @Parameter(description = "Report type filter")
    @Schema(description = "Report type filter", example = "FINAL_INSPECTION")
    private String reportType;

    @Size(max = 50, message = "Status must not exceed 50 characters")
    @Parameter(description = "Status filter")
    @Schema(description = "Status filter", example = "APPROVED")
    private String status;

    @Parameter(description = "Shift ID filter")
    @Schema(description = "Shift ID filter", example = "1")
    private Long shiftId;

    @Parameter(description = "Line ID filter")
    @Schema(description = "Line ID filter", example = "1")
    private Long lineId;

    @Parameter(description = "Start date filter")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "Start date filter", example = "2025-01-01")
    private LocalDate dateFrom;

    @Parameter(description = "End date filter")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "End date filter", example = "2025-12-31")
    private LocalDate dateTo;

    @Size(max = 500, message = "Keyword must not exceed 500 characters")
    @Parameter(description = "Keyword search filter")
    @Schema(description = "Keyword search filter", example = "defect")
    private String keyword;

}
