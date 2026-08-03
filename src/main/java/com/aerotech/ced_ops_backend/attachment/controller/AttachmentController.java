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
 *  io.swagger.v3.oas.annotations.tags.Tag
 *  jakarta.validation.Valid
 *  jakarta.validation.constraints.Min
 *  lombok.Generated
 *  org.springframework.core.io.Resource
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.MediaType
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.security.access.prepost.PreAuthorize
 *  org.springframework.web.bind.annotation.DeleteMapping
 *  org.springframework.web.bind.annotation.GetMapping
 *  org.springframework.web.bind.annotation.PathVariable
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.PutMapping
 *  org.springframework.web.bind.annotation.RequestBody
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestParam
 *  org.springframework.web.bind.annotation.RestController
 *  org.springframework.web.multipart.MultipartFile
 */
package com.aerotech.ced_ops_backend.attachment.controller;

import com.aerotech.ced_ops_backend.attachment.dto.request.UpdateAttachmentRequest;
import com.aerotech.ced_ops_backend.attachment.dto.response.AttachmentResponse;
import com.aerotech.ced_ops_backend.attachment.service.AttachmentService;
import com.aerotech.ced_ops_backend.attachment.service.StorageService;
import com.aerotech.ced_ops_backend.common.response.ApiError;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import lombok.Generated;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value={"/api/attachments"})
@Tag(name="Attachment Management", description="File attachment management APIs")
public class AttachmentController {
    private final AttachmentService attachmentService;
    private final StorageService storageService;

    @PostMapping(value={"/upload"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Upload a single file attachment", description="Uploads a single file as an attachment and returns its metadata. Supports multipart/form-data.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Multipart form data containing the file to upload and optional metadata", required=true, content={@Content(mediaType="multipart/form-data")}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="File uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - invalid file or missing parameters", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<AttachmentResponse>> upload(@RequestParam(value="file") @Parameter(description="The file to upload", example="report.pdf", required=true) MultipartFile file, @RequestParam(required=false) @Parameter(description="Related module name", example="quality-inspection") String relatedModule, @RequestParam(required=false) @Parameter(description="Related entity ID", example="REP-001234") String relatedEntityId, @RequestParam(required=false) @Parameter(description="Attachment category", example="INSPECTION_IMAGE", schema=@Schema(allowableValues={"REPORT_ATTACHMENT", "INSPECTION_IMAGE", "SUPPORTING_DOCUMENT", "SIGNATURE", "OTHER"})) String category, @RequestParam(required=false) @Parameter(description="Description", example="Photo of the final inspection result") String description) {
        AttachmentResponse response = this.attachmentService.upload(file, relatedModule, relatedEntityId, category, description);
        URI location = URI.create("/api/attachments/" + response.getId());
        return ResponseEntity.created((URI)location).body(ApiResponse.<AttachmentResponse>builder().success(true).message("File uploaded successfully.").data(response).build());
    }

    @PostMapping(value={"/upload-multiple"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Upload multiple file attachments", description="Uploads multiple files as attachments in a single request and returns their metadata. Supports multipart/form-data.", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Multipart form data containing the files to upload and optional metadata", required=true, content={@Content(mediaType="multipart/form-data")}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="201", description="Files uploaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - invalid files or missing parameters", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadMultiple(@RequestParam(value="files") @Parameter(description="The files to upload", example="report.pdf", required=true) List<MultipartFile> files, @RequestParam(required=false) @Parameter(description="Related module name", example="quality-inspection") String relatedModule, @RequestParam(required=false) @Parameter(description="Related entity ID", example="REP-001234") String relatedEntityId, @RequestParam(required=false) @Parameter(description="Attachment category", example="INSPECTION_IMAGE", schema=@Schema(allowableValues={"REPORT_ATTACHMENT", "INSPECTION_IMAGE", "SUPPORTING_DOCUMENT", "SIGNATURE", "OTHER"})) String category, @RequestParam(required=false) @Parameter(description="Description", example="Photo of the final inspection result") String description) {
        List<AttachmentResponse> responses = this.attachmentService.uploadMultiple(files, relatedModule, relatedEntityId, category, description);
        return ResponseEntity.status((HttpStatusCode)HttpStatus.CREATED).body(ApiResponse.<List<AttachmentResponse>>builder().success(true).message("Files uploaded successfully.").data(responses).build());
    }

    @GetMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get attachment metadata by ID", description="Returns the metadata of a single attachment by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Attachment metadata retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Attachment not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<AttachmentResponse>> getById(@PathVariable @Parameter(description="Attachment ID", example="1", required=true) Long id) {
        return ResponseEntity.ok(ApiResponse.<AttachmentResponse>builder().success(true).message("Attachment fetched successfully.").data(this.attachmentService.getById(id)).build());
    }

    @GetMapping(value={"/entity/{module}/{entityId}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Get all attachments for a specific entity", description="Returns all attachments linked to the given module and entity ID")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Attachments retrieved for the entity"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getByEntity(@PathVariable @Parameter(description="Module name", example="quality-inspection", required=true) String module, @PathVariable @Parameter(description="Entity ID", example="REP-001234", required=true) String entityId) {
        return ResponseEntity.ok(ApiResponse.<List<AttachmentResponse>>builder().success(true).message("Attachments fetched successfully.").data(this.attachmentService.getByEntity(module, entityId)).build());
    }

    @PutMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Update attachment metadata", description="Updates the metadata of an existing attachment by its unique identifier", requestBody=@io.swagger.v3.oas.annotations.parameters.RequestBody(description="Payload to update attachment metadata", required=true, content={@Content(mediaType="application/json", schema=@Schema(implementation=UpdateAttachmentRequest.class), examples={@ExampleObject(name="UpdateAttachmentRequestExample", summary="Example payload to update attachment metadata", value="{\"category\":\"INSPECTION_IMAGE\",\"description\":\"Photo of the final inspection result\",\"relatedModule\":\"quality-inspection\",\"relatedEntityId\":\"REP-001234\"}")})}))
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Attachment updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request body", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Attachment not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<AttachmentResponse>> update(@PathVariable @Parameter(description="Attachment ID", example="1", required=true) Long id, @Valid @RequestBody UpdateAttachmentRequest request) {
        return ResponseEntity.ok(ApiResponse.<AttachmentResponse>builder().success(true).message("Attachment updated successfully.").data(this.attachmentService.update(id, request)).build());
    }

    @DeleteMapping(value={"/{id}"}, produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Soft delete an attachment", description="Soft deletes an attachment by its unique identifier")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="204", description="Attachment soft-deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Attachment not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Void> softDelete(@PathVariable @Parameter(description="Attachment ID", example="1", required=true) Long id) {
        this.attachmentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value={"/download/{id}"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Download an attachment file", description="Downloads the stored file of an attachment as a binary stream with a Content-Disposition attachment header")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="File downloaded successfully", content={@Content(mediaType="application/octet-stream", schema=@Schema(type="string", format="binary"))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Attachment not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Resource> download(@PathVariable @Parameter(description="Attachment ID", example="1", required=true) Long id) throws IOException {
        AttachmentResponse attachment = this.attachmentService.getAttachment(id);
        Resource resource = this.storageService.loadAsResource(attachment.getStoredFileName());
        String contentType = attachment.getMimeType() != null ? attachment.getMimeType() : "application/octet-stream";
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().contentType(MediaType.parseMediaType((String)contentType)).header("Content-Disposition", new String[]{"attachment; filename=\"" + attachment.getOriginalFileName() + "\""})).body(resource);
    }

    @GetMapping(value={"/preview/{id}"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Preview an attachment inline for images and PDFs", description="Streams the stored file of an attachment inline for images and PDFs with a Content-Disposition inline header")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="File preview generated", content={@Content(mediaType="application/octet-stream", schema=@Schema(type="string", format="binary"))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="404", description="Attachment not found", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<Resource> preview(@PathVariable @Parameter(description="Attachment ID", example="1", required=true) Long id) throws IOException {
        AttachmentResponse attachment = this.attachmentService.getAttachment(id);
        Resource resource = this.storageService.loadAsResource(attachment.getStoredFileName());
        String contentType = attachment.getMimeType() != null ? attachment.getMimeType() : "application/octet-stream";
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().contentType(MediaType.parseMediaType((String)contentType)).header("Content-Disposition", new String[]{"inline"})).body(resource);
    }

    @GetMapping(produces={"application/json"})
    @PreAuthorize(value="isAuthenticated()")
    @SecurityRequirement(name="bearerAuth")
    @Operation(summary="Search attachments with pagination", description="Returns a paginated list of attachments, optionally filtered by keyword, module and category")
    @ApiResponses(value={
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="200", description="Attachments fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="400", description="Bad Request - validation error or malformed request", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="401", description="Unauthorized - authentication required", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))}),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode="500", description="Internal server error", content={@Content(mediaType="application/json", schema=@Schema(implementation=ApiError.class))})
    })
    public ResponseEntity<ApiResponse<PageResponse<AttachmentResponse>>> search(@RequestParam(required=false) @Parameter(description="Search keyword in file name", example="report") String keyword, @RequestParam(required=false) @Parameter(description="Filter by module", example="quality-inspection") String relatedModule, @RequestParam(required=false) @Parameter(description="Filter by category", example="INSPECTION_IMAGE", schema=@Schema(allowableValues={"REPORT_ATTACHMENT", "INSPECTION_IMAGE", "SUPPORTING_DOCUMENT", "SIGNATURE", "OTHER"})) String category, @RequestParam(defaultValue="0") @Min(value=0L) @Parameter(description="Page number", example="0") @Min(value=0L) int page, @RequestParam(defaultValue="20") @Min(value=1L) @Parameter(description="Page size", example="20") @Min(value=1L) int size) {
        return ResponseEntity.ok(ApiResponse.<PageResponse<AttachmentResponse>>builder().success(true).message("Attachments fetched successfully.").data(this.attachmentService.search(keyword, relatedModule, category, page, size)).build());
    }

    @Generated
    public AttachmentController(AttachmentService attachmentService, StorageService storageService) {
        this.attachmentService = attachmentService;
        this.storageService = storageService;
    }
}
