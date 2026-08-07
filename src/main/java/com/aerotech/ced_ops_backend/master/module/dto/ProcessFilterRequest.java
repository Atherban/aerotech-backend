package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.common.pagination.PageRequest;
import com.aerotech.ced_ops_backend.master.module.enums.ProcessStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Filter for paginated process listing")
public class ProcessFilterRequest extends PageRequest {

    @Schema(description = "Filter by owning template version ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long templateVersionId;

    @Schema(description = "Filter by lifecycle status", example = "ACTIVE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ProcessStatus status;

    @Override
    public boolean hasSearchCriteria() {
        return super.hasSearchCriteria() || templateVersionId != null || status != null;
    }

}