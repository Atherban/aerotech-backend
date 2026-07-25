package com.aerotech.ced_ops_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Old Password is required")
    @Schema(description = "Current password", example = "oldPassword123")
    private String oldPassword;

    @NotBlank(message = "New Password is required")
    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Schema(description = "New password (minimum 8 characters)", example = "newPassword123")
    private String newPassword;

}