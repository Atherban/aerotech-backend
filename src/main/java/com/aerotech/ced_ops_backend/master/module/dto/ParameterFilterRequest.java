package com.aerotech.ced_ops_backend.master.module.dto;

import com.aerotech.ced_ops_backend.common.enums.InputType;
import com.aerotech.ced_ops_backend.common.pagination.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Filter for paginated global parameter listing")
public class ParameterFilterRequest extends PageRequest {

    @Schema(description = "Filter by input type", example = "NUMBER", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private InputType inputType;

    @Schema(description = "Filter by active status", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

    @Override
    public boolean hasSearchCriteria() {
        return super.hasSearchCriteria() || inputType != null || active != null;
    }

}