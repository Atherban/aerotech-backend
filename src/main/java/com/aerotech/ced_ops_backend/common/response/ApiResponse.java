package com.aerotech.ced_ops_backend.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard API response wrapper")
public class ApiResponse<T> {
    @Schema(description = "Indicates if the request was successful", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;

    @Schema(description = "Response message", example = "Operation completed successfully", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String message;

    @Schema(description = "Response payload", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private T data;
}
