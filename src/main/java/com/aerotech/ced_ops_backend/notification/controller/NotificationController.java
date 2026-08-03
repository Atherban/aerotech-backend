/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.Operation
 *  io.swagger.v3.oas.annotations.Parameter
 *  io.swagger.v3.oas.annotations.media.Content
 *  io.swagger.v3.oas.annotations.media.ExampleObject
 *  io.swagger.v3.oas.annotations.media.Schema
 *  io.swagger.v3.oas.annotations.responses.ApiResponse
 *  io.swagger.v3.oas.annotations.responses.ApiResponses
 *  io.swagger.v3.oas.annotations.security.SecurityRequirement
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  jakarta.validation.constraints.Min
 *  lombok.Generated
 *  org.springframework.http.ResponseEntity
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PatchMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 */
package com.aerotech.ced_ops_backend.notification.controller;

import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.notification.dto.response.NotificationResponse;
import com.aerotech.ced_ops_backend.notification.dto.response.UnreadCountResponse;
import com.aerotech.ced_ops_backend.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.Generated;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value={"/api/notifications"})
@Tag(name="Notifications", description="In-app notification management APIs")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get my notifications with pagination, type and read-status filtering", description="Returns a paginated list of notifications for the current user, optionally filtered by type and read status")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Notifications fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(@RequestParam(required=false) @Parameter(description="Filter by type", example="REPORT_APPROVED", schema=@Schema(allowableValues={"WELCOME", "PASSWORD_CHANGED", "REPORT_CREATED", "REPORT_SUBMITTED", "REPORT_APPROVED", "REPORT_REJECTED", "REPORT_RETURNED", "PENDING_APPROVAL", "APPROVAL_REMINDER", "USER_CREATED", "USER_ACTIVATED", "USER_DEACTIVATED", "ROLE_CHANGED", "ATTACHMENT_UPLOADED", "MAINTENANCE_NOTICE"})) String type, @RequestParam(required=false) @Parameter(description="Filter by read status", example="false") Boolean isRead, @RequestParam(defaultValue="0") @Min(value=0L) @Parameter(description="Page number", example="0") @Min(value=0L) int page, @RequestParam(defaultValue="20") @Min(value=1L) @Parameter(description="Page size", example="20") @Min(value=1L) int size) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<NotificationResponse>>builder().success(true).message("Notifications fetched successfully.").data(this.notificationService.getNotifications(type, isRead, page, size)).build());
    }

    @GetMapping(value={"/unread"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get all unread notifications for the current user", description="Returns a list of all unread notifications for the current user")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Unread notifications fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnreadNotifications() {
        return ResponseEntity.ok(ApiResponse.<List<NotificationResponse>>builder().success(true).message("Unread notifications fetched successfully.").data(this.notificationService.getUnreadNotifications()).build());
    }

    @GetMapping(value={"/count"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get unread notification count", description="Returns the number of unread notifications for the current user")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Unread count fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.<UnreadCountResponse>builder().success(true).message("Unread count fetched successfully.").data(this.notificationService.getUnreadCount()).build());
    }

    @PatchMapping(value={"/{id}/read"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Mark a single notification as read", description="Marks a single notification as read by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Notification marked as read"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Notification not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@Parameter(description="Notification ID", example="1", required=true) @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<NotificationResponse>builder().success(true).message("Notification marked as read.").data(this.notificationService.markAsRead(id)).build());
    }

    @PatchMapping(value={"/read-all"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Mark all notifications as read for the current user", description="Marks all notifications of the current user as read")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="All notifications marked as read"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        this.notificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("All notifications marked as read.").build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Delete a single notification", description="Deletes a single notification by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Notification deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Notification not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> delete(@Parameter(description="Notification ID", example="1", required=true) @PathVariable Long id) {
        this.notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Generated
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
