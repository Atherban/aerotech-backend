package com.aerotech.ced_ops_backend.report.predeliveryinspection.dto.response;

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
@Schema(description = "Response payload for a Pre-Delivery Inspection report")
public class PreDeliveryInspectionResponse {

    @Schema(description = "Unique identifier of the report", example = "1")
    private Long id;

    @Schema(description = "Auto-generated report number", example = "PDI-2025-0001")
    private String reportNumber;

    @Schema(description = "Date of the inspection", example = "2025-01-15")
    private LocalDate reportDate;

    @Schema(description = "Shift name", example = "Morning")
    private String shift;

    @Schema(description = "Production line name", example = "Line A")
    private String line;

    @Schema(description = "Product part number", example = "PART-001")
    private String productPartNumber;

    @Schema(description = "Batch number", example = "BATCH-001")
    private String batchNumber;

    @Schema(description = "Inspector name", example = "Jane Smith")
    private String inspectorName;

    @Schema(description = "Employee who created the report", example = "jdoe")
    private String createdBy;

    @Schema(description = "Employee who approved the report", example = "asmith")
    private String approvedBy;

    @Schema(description = "Current status of the report")
    private ReportStatus status;

    @Schema(description = "Additional remarks", example = "All checks completed")
    private String remarks;

    @Schema(description = "Timestamp when the report was approved", example = "2025-01-15T10:30:00")
    private LocalDateTime approvedAt;

    @Schema(description = "Timestamp when the report was created", example = "2025-01-15T08:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "List of inspection entries")
    private List<PreDeliveryInspectionEntryResponse> entries;

}
