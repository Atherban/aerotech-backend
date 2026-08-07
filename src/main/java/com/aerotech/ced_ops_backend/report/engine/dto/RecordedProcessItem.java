package com.aerotech.ced_ops_backend.report.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * A recorded process including its grouped recorded values.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A recorded process and its grouped values")
public class RecordedProcessItem {

    @Schema(description = "Recorded process ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Template process ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long processId;

    @Schema(description = "Process name (from the frozen template)", example = "CED Coating", requiredMode = Schema.RequiredMode.REQUIRED)
    private String processName;

    @Schema(description = "displayOrder snapshot at recording time", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer processOrderSnapshot;

    @Schema(description = "Grouped recorded values", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<RecordedValueItem> values;

}