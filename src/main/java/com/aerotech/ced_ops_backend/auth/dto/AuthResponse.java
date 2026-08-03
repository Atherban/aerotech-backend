package com.aerotech.ced_ops_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT tokens and user details")
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String accessToken;

    @Schema(description = "Refresh token for obtaining a new access token", example = "dGhpcyBpcyBhIHJlZnJl...", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String refreshToken;

    @Schema(description = "Token type", example = "Bearer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String tokenType;

    @Schema(description = "Employee ID", example = "EMP001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String employeeId;

    @Schema(description = "Full name of the user", example = "John Doe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fullName;

    @Schema(description = "User role", example = "SUPER_ADMIN", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String role;
}
