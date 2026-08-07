package com.aerotech.ced_ops_backend.report.engine.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * The process the backend instructs the frontend to render next, including its
 * fillable fields. The backend fully controls navigation via displayOrder.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The current/next process the frontend should render")
public class ReportProcessStep {

    @Schema(description = "Process ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long processId;

    @Schema(description = "Process name", example = "CED Coating", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Process description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "displayOrder (the only ordering mechanism)", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    @Schema(description = "Whether this is the final process of the template", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean lastProcess;

    @Schema(description = "Fillable fields, ordered by displayOrder", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ProcessParameterField> fields;

}