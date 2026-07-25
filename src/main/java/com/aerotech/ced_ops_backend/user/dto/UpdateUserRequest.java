package com.aerotech.ced_ops_backend.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "First Name is required")
    @Schema(description = "First name", example = "John")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    @Schema(description = "Last name", example = "Doe")
    private String lastName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Mobile Number")
    @Schema(description = "Mobile number (10 digits, starting with 6-9)", example = "9876543210")
    private String mobileNumber;

    @NotBlank(message = "Role is required")
    @Schema(description = "User role", example = "OPERATOR", allowableValues = {"SUPER_ADMIN", "ADMIN", "OPERATOR", "INSPECTOR"})
    private String role;

}