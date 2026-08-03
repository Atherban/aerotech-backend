package com.aerotech.ced_ops_backend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Login request payload")
public class LoginRequest {

    @NotBlank(message = "Employee ID is required")
    @Schema(description = "Employee ID", example = "EMP001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String employeeId;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
