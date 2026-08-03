package com.aerotech.ced_ops_backend.report.firstpieceinspection.dto.response;

import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload for a First Piece Inspection report")
public class FirstPieceInspectionResponse {

    @Schema(description = "Unique identifier of the report", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Auto-generated report number", example = "FPI-2025-0001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String reportNumber;

    @Schema(description = "Date of the inspection", example = "2025-01-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate reportDate;

    @Schema(description = "Shift name", example = "Morning", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String shift;

    @Schema(description = "Production line name", example = "Line A", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String line;

    @Schema(description = "Product casting number", example = "CAST-001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String productCastingNumber;

    @Schema(description = "Operator name", example = "John Doe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String operatorName;

    @Schema(description = "Inspector name", example = "Jane Smith", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String inspectorName;

    @Schema(description = "Employee who created the report", example = "jdoe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String createdBy;

    @Schema(description = "Employee who approved the report", example = "asmith", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String approvedBy;

    @Schema(description = "Current status of the report", example = "APPROVED", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ReportStatus status;

    @Schema(description = "Additional remarks", example = "All measurements within tolerance", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String remarks;

    @Schema(description = "Timestamp when the report was approved", example = "2025-01-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime approvedAt;

    @Schema(description = "Timestamp when the report was created", example = "2025-01-15T08:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "List of inspection entries", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<FirstPieceInspectionEntryResponse> entries;

}
