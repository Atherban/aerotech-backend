package com.aerotech.ced_ops_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Employee ID is required")
    @Schema(description = "Unique employee ID", example = "EMP001")
    private String employeeId;

    @NotBlank(message = "First Name is required")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Mobile Number")
    @Schema(description = "Mobile number (10 digits, starting with 6-9)", example = "9876543210")
    private String mobileNumber;

    @Size(min = 8, message = "Password must contain at least 8 characters")
    @Schema(description = "Password (minimum 8 characters)", example = "password123")
    private String password;

    @NotBlank(message = "Role is required")
    @Schema(description = "User role", example = "OPERATOR", allowableValues = {"SUPER_ADMIN", "ADMIN", "OPERATOR", "INSPECTOR"})
    private String role;

}