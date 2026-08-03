package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Category of an attached file. Values: REPORT_ATTACHMENT - General report attachment; INSPECTION_IMAGE - Inspection image; SUPPORTING_DOCUMENT - Supporting document; SIGNATURE - Digital signature image; OTHER - Other attachment types")
public enum AttachmentCategory {
    REPORT_ATTACHMENT, // General report attachment
    INSPECTION_IMAGE, // Inspection image
    SUPPORTING_DOCUMENT, // Supporting document
    SIGNATURE, // Digital signature image
    OTHER // Other attachment types
}
