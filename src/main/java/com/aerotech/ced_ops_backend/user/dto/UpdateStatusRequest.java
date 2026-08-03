package com.aerotech.ced_ops_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for updating a user's active status")
public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    @Schema(description = "New active status", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean active;

}