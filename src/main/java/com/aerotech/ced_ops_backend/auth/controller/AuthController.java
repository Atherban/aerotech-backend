/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.parameters.RequestBody
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.security.SecurityRequirements
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  jakarta.validation.Valid
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.security.core.Authentication
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestHeader
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RestController
 */
package com.aerotech.ced_ops_backend.auth.controller;

import com.aerotech.ced_ops_backend.auth.dto.AuthResponse;
import com.aerotech.ced_ops_backend.auth.dto.LoginRequest;
import com.aerotech.ced_ops_backend.auth.dto.RefreshTokenRequest;
import com.aerotech.ced_ops_backend.auth.service.AuthService;
import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/auth"})
@Tag(name="Authentication", description="User authentication and token management APIs")
public class AuthController {
    private final AuthService authService;

    @PostMapping(value={"/login"}, produces={"application/json"})
    @SecurityRequirements(value={})
    @Operation(summary="Authenticate user and return JWT tokens", description="Public endpoint. Authenticates a user using employee ID and password and returns a JWT access token along with a refresh token.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Login credentials", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=LoginRequest.class), examples={@ExampleObject(name="LoginRequestExample", summary="Example login request", value="{\"employeeId\": \"EMP001\", \"password\": \"password123\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Login successful, returns JWT tokens"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - invalid employee ID or password", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder().success(true).message("Login Successful").data(this.authService.login(request)).build());
    }

    @PostMapping(value={"/refresh"}, produces={"application/json"})
    @SecurityRequirements(value={})
    @Operation(summary="Refresh expired access token using refresh token", description="Public endpoint. Exchanges a valid refresh token for a new JWT access token.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Refresh token issued during login", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=RefreshTokenRequest.class), examples={@ExampleObject(name="RefreshTokenRequestExample", summary="Example refresh token request", value="{\"refreshToken\": \"dGhpcyBpcyBhIHJlZnJl...\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Token refreshed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - invalid or expired refresh token", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder().success(true).message("Token Refreshed").data(this.authService.refreshToken(request)).build());
    }

    @PostMapping(value={"/logout"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Invalidate refresh token and logout user", description="Invalidates the refresh token of the currently authenticated user, effectively logging them out.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Logout successful"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<Void>> logout(Authentication authentication) {
        this.authService.logout(authentication.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Logout Successful").build());
    }

    @GetMapping(value={"/me"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get current authenticated user's profile", description="Retrieves the profile of the currently authenticated user.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="User not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder().success(true).message("Current User").data(this.authService.me(authentication.getName())).build());
    }

    @GetMapping(value={"/validate"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Validate JWT token validity", description="Checks whether the supplied JWT access token is valid and not expired.")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Token is valid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<Boolean>> validate(@Parameter(description="Authorization header containing the Bearer token", example="Bearer eyJhbGciOiJIUzI1NiJ9...", required=false) @RequestHeader(value="Authorization", required=false, defaultValue="") String authorization) {
        String token = authorization.length() > 7 ? authorization.substring(7) : "";
        return ResponseEntity.ok(ApiResponse.<Boolean>builder().success(true).message("Token Valid").data(this.authService.validateToken(token)).build());
    }

    @Generated
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
}
