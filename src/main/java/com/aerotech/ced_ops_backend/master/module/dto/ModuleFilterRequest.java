package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.common.pagination.PageRequest;
import com.aerotech.ced_ops_backend.master.module.enums.ModuleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Filter for paginated module listing")
public class ModuleFilterRequest extends PageRequest {

    @Schema(description = "Filter by module type ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long moduleTypeId;

    @Schema(description = "Filter by lifecycle status", example = "ACTIVE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ModuleStatus status;

    @Override
    public boolean hasSearchCriteria() {
        return super.hasSearchCriteria() || moduleTypeId != null || status != null;
    }

}