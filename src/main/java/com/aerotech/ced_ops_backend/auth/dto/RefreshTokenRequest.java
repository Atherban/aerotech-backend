package com.aerotech.ced_ops_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Refresh token request payload")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token issued during login", example = "dGhpcyBpcyBhIHJlZnJl...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;

}
