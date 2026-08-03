package com.aerotech.ced_ops_backend.report.globalsearch.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Search suggestions for autocomplete")
public class SearchSuggestionsResponse {

    @Schema(description = "List of report number suggestions", example = "[\"FPI-2025-0001\", \"FPI-2025-0002\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> reportNumbers;

    @Schema(description = "List of employee name suggestions", example = "[\"John Doe\", \"Jane Smith\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> employeeNames;

    @Schema(description = "List of production line suggestions", example = "[\"Line A\", \"Line B\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> lines;

    @Schema(description = "List of parameter name suggestions", example = "[\"Bath Temperature\", \"Voltage\"]", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> parameters;

}
