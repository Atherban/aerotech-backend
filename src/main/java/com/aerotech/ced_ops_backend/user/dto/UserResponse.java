package com.aerotech.ced_ops_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User details response")
public class UserResponse {

    @Schema(description = "User ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Employee ID", example = "EMP001", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String employeeId;

    @Schema(description = "First name", example = "John", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String firstName;

    @Schema(description = "Last name", example = "Doe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String lastName;

    @Schema(description = "Mobile number", example = "9876543210", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String mobileNumber;

    @Schema(description = "User role", example = "OPERATOR", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String role;

    @Schema(description = "Whether the user account is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean active;

    @Schema(description = "Account creation timestamp", example = "2026-07-01T09:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-08-02T10:00:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime updatedAt;

}