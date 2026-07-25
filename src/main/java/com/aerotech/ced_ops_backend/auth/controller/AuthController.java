package com.aerotech.ced_ops_backend.auth.controller;

import com.aerotech.ced_ops_backend.auth.dto.AuthResponse;
import com.aerotech.ced_ops_backend.user.dto.UserResponse;
import com.aerotech.ced_ops_backend.auth.dto.LoginRequest;
import com.aerotech.ced_ops_backend.auth.dto.RefreshTokenRequest;
import com.aerotech.ced_ops_backend.auth.service.AuthService;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and token management APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT tokens")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login successful, returns JWT tokens"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login Successful")
                        .data(authService.login(request))
                        .build()
        );

    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh expired access token using refresh token")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid or expired refresh token")
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Token Refreshed")
                        .data(authService.refreshToken(request))
                        .build()

        );

    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate refresh token and logout user")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful")
    public ResponseEntity<ApiResponse<Void>> logout(
            Authentication authentication
    ) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Logout Successful")
                        .build()

        );

    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current authenticated user's profile")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ApiResponse<UserResponse>> me(
            Authentication authentication
    ){

        return ResponseEntity.ok(
                ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Current User")
                        .data(
                                authService.me(authentication.getName())
                        )
                        .build()

        );

    }

    @GetMapping("/validate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Validate JWT token validity")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Token is valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    public ResponseEntity<ApiResponse<Boolean>> validate(
            @RequestHeader("Authorization") String authorization
    ){

        String token = authorization.substring(7);

        return ResponseEntity.ok(
                ApiResponse.<Boolean>builder()
                        .success(true)
                        .message("Token Valid")
                        .data(
                                authService.validateToken(token)
                        )
                        .build()

        );
    }

}
