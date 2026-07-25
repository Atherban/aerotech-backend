package com.aerotech.ced_ops_backend.attachment.controller;

import com.aerotech.ced_ops_backend.attachment.dto.request.UpdateAttachmentRequest;
import com.aerotech.ced_ops_backend.attachment.dto.response.AttachmentResponse;
import com.aerotech.ced_ops_backend.attachment.service.AttachmentService;
import com.aerotech.ced_ops_backend.attachment.service.StorageService;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment Management", description = "File attachment management APIs")
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final StorageService storageService;

    @PostMapping("/upload")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "File uploaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file or missing parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Operation(summary = "Upload a single file attachment")
    public ResponseEntity<ApiResponse<AttachmentResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) @Parameter(description = "Related module name") String relatedModule,
            @RequestParam(required = false) @Parameter(description = "Related entity ID") String relatedEntityId,
            @RequestParam(required = false) @Parameter(description = "Attachment category") String category,
            @RequestParam(required = false) @Parameter(description = "Description") String description
    ) {
        AttachmentResponse response = attachmentService.upload(file, relatedModule, relatedEntityId, category, description);
        URI location = URI.create("/api/attachments/" + response.getId());
        return ResponseEntity.created(location)
                .body(ApiResponse.<AttachmentResponse>builder()
                        .success(true)
                        .message("File uploaded successfully.")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/upload-multiple")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Files uploaded successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid files or missing parameters"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Operation(summary = "Upload multiple file attachments")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadMultiple(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(required = false) @Parameter(description = "Related module name") String relatedModule,
            @RequestParam(required = false) @Parameter(description = "Related entity ID") String relatedEntityId,
            @RequestParam(required = false) @Parameter(description = "Attachment category") String category,
            @RequestParam(required = false) @Parameter(description = "Description") String description
    ) {
        List<AttachmentResponse> responses = attachmentService.uploadMultiple(files, relatedModule, relatedEntityId, category, description);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<List<AttachmentResponse>>builder()
                        .success(true)
                        .message("Files uploaded successfully.")
                        .data(responses)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attachment metadata retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @Operation(summary = "Get attachment metadata by ID")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getById(
            @PathVariable @Parameter(description = "Attachment ID") Long id
    ) {
        return ResponseEntity.ok(
                ApiResponse.<AttachmentResponse>builder()
                        .success(true)
                        .message("Attachment fetched successfully.")
                        .data(attachmentService.getById(id))
                        .build()
        );
    }

    @GetMapping("/entity/{module}/{entityId}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attachments retrieved for the entity")
    })
    @Operation(summary = "Get all attachments for a specific entity")
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getByEntity(
            @PathVariable @Parameter(description = "Module name") String module,
            @PathVariable @Parameter(description = "Entity ID") String entityId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<List<AttachmentResponse>>builder()
                        .success(true)
                        .message("Attachments fetched successfully.")
                        .data(attachmentService.getByEntity(module, entityId))
                        .build()
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attachment updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @Operation(summary = "Update attachment metadata")
    public ResponseEntity<ApiResponse<AttachmentResponse>> update(
            @PathVariable @Parameter(description = "Attachment ID") Long id,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateAttachmentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<AttachmentResponse>builder()
                        .success(true)
                        .message("Attachment updated successfully.")
                        .data(attachmentService.update(id, request))
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Attachment soft-deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @Operation(summary = "Soft delete an attachment")
    public ResponseEntity<Void> softDelete(
            @PathVariable @Parameter(description = "Attachment ID") Long id
    ) {
        attachmentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @Operation(summary = "Download an attachment file")
    public ResponseEntity<Resource> download(
            @PathVariable @Parameter(description = "Attachment ID") Long id
    ) throws IOException {
        AttachmentResponse attachment = attachmentService.getAttachment(id);
        Resource resource = storageService.loadAsResource(attachment.getStoredFileName());

        String contentType = attachment.getMimeType() != null
                ? attachment.getMimeType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getOriginalFileName() + "\"")
                .body(resource);
    }

    @GetMapping("/preview/{id}")
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "File preview generated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Attachment not found")
    })
    @Operation(summary = "Preview an attachment inline for images and PDFs")
    public ResponseEntity<Resource> preview(
            @PathVariable @Parameter(description = "Attachment ID") Long id
    ) throws IOException {
        AttachmentResponse attachment = attachmentService.getAttachment(id);
        Resource resource = storageService.loadAsResource(attachment.getStoredFileName());

        String contentType = attachment.getMimeType() != null
                ? attachment.getMimeType() : "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearerAuth")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Attachments fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @Operation(summary = "Search attachments with pagination")
    public ResponseEntity<ApiResponse<PageResponse<AttachmentResponse>>> search(
            @RequestParam(required = false) @Parameter(description = "Search keyword in file name") String keyword,
            @RequestParam(required = false) @Parameter(description = "Filter by module") String relatedModule,
            @RequestParam(required = false) @Parameter(description = "Filter by category") String category,
            @RequestParam(defaultValue = "0") @Min(0) @Parameter(description = "Page number") int page,
            @RequestParam(defaultValue = "20") @Min(1) @Parameter(description = "Page size") int size
    ) {
        return ResponseEntity.ok(
                ApiResponse.<PageResponse<AttachmentResponse>>builder()
                        .success(true)
                        .message("Attachments fetched successfully.")
                        .data(attachmentService.search(keyword, relatedModule, category, page, size))
                        .build()
        );
    }

}
