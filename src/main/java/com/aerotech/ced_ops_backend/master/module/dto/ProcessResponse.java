package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.master.module.enums.ProcessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Process data")
public class ProcessResponse {

    @Schema(description = "Process ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Owning template version ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long templateVersionId;

    @Schema(description = "Process name", example = "CED Coating", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Optional description", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "The ONLY ordering mechanism", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer displayOrder;

    @Schema(description = "Lifecycle status", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private ProcessStatus status;

}