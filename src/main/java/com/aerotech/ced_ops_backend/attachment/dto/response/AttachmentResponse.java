package com.aerotech.ced_ops_backend.attachment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing attachment details")
public class AttachmentResponse {

    @Schema(description = "Attachment ID", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long id;

    @Schema(description = "Original file name", example = "report.pdf", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String originalFileName;

    @Schema(description = "Stored file name on server", example = "abc12345_report.pdf", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String storedFileName;

    @Schema(description = "File extension", example = "pdf", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fileExtension;

    @Schema(description = "MIME type of the file", example = "application/pdf", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String mimeType;

    @Schema(description = "File size in bytes", example = "204800", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long fileSize;

    @Schema(description = "File hash for integrity verification", example = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String fileHash;

    @Schema(description = "Username who uploaded the file", example = "jdoe", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String uploadedBy;

    @Schema(description = "Upload timestamp", example = "2025-06-15T10:30:00", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDateTime uploadedAt;

    @Schema(description = "Related module name", example = "quality-inspection", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String relatedModule;

    @Schema(description = "Related entity ID", example = "REP-001234", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String relatedEntityId;

    @Schema(description = "Attachment category", example = "INSPECTION_IMAGE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String category;

    @Schema(description = "Description of the attachment", example = "Photo of the final inspection result", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(description = "Whether the attachment is active", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isActive;

}
