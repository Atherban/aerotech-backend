package com.aerotech.ced_ops_backend.analytics.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "A single trend data point with a date and value")
public class TrendPoint {

    @Schema(description = "Date of the data point", example = "2025-06-15", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate date;

    @Schema(description = "Value at this date", example = "42", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long value;

}
