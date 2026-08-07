package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.common.pagination.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Filter for paginated module type listing")
public class ModuleTypeFilterRequest extends PageRequest {

    @Schema(description = "Filter by active status", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

    @Override
    public boolean hasSearchCriteria() {
        return super.hasSearchCriteria() || active != null;
    }

}