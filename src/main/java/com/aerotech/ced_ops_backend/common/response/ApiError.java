package com.aerotech.ced_ops_backend.common.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API error response envelope returned on all failed requests")
public class ApiError {

    @Schema(
            description = "Indicates whether the operation was successful",
            example = "false",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean success;

    @Schema(
            description = "HTTP status code of the error response",
            example = "400",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int status;

    @Schema(
            description = "Human-readable error message summarizing the problem",
            example = "Validation Failed",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String message;

    @Schema(
            description = "Timestamp when the error occurred",
            example = "2026-07-24T14:30:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime timestamp;

    @ArraySchema(
            arraySchema = @Schema(
                    description = "List of individual error messages (e.g., field validation errors)",
                    example = "[\"Field 'email' must not be blank\"]"
            ),
            schema = @Schema(
                    implementation = String.class,
                    description = "A single field-validation or business error message"
            )
    )
    private List<String> errors;

    @Schema(
            description = "Additional error-related data or payload",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Object data;
}
