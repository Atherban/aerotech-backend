package com.aerotech.ced_ops_backend.attachment.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request to update an existing attachment's metadata")
public class UpdateAttachmentRequest {

    @Size(max = 100, message = "Category must not exceed 100 characters")
    @Parameter(description = "Attachment category")
    @Schema(description = "Attachment category", example = "INSPECTION_IMAGE", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String category;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Parameter(description = "Description of the attachment")
    @Schema(description = "Description of the attachment", example = "Photo of the final inspection result", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Size(max = 100, message = "Module name must not exceed 100 characters")
    @Parameter(description = "Related module name")
    @Schema(description = "Related module name", example = "quality-inspection", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String relatedModule;

    @Size(max = 100, message = "Entity ID must not exceed 100 characters")
    @Parameter(description = "Related entity ID")
    @Schema(description = "Related entity ID", example = "REP-001234", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String relatedEntityId;

}
