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
            example = "false"
    )
    private boolean success;

    @Schema(
            description = "HTTP status code of the error response",
            example = "400"
    )
    private int status;

    @Schema(
            description = "Human-readable error message summarizing the problem",
            example = "Validation Failed"
    )
    private String message;

    @Schema(
            description = "Timestamp when the error occurred",
            example = "2026-07-24T14:30:00"
    )
    private LocalDateTime timestamp;

    @ArraySchema(
            schema = @Schema(
                    description = "List of individual error messages (e.g., field validation errors)",
                    example = "Field 'email' must not be blank"
            )
    )
    private List<String> errors;

    @Schema(
            description = "Additional error-related data or payload",
            nullable = true
    )
    private Object data;
}
