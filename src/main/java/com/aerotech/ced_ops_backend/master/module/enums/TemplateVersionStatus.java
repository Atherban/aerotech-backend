package com.aerotech.ced_ops_backend.master.module.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lifecycle status of a Module template version. Values: DRAFT - being edited, not yet serving reports; ACTIVE - the version new reports are created against; SUPERSEDED - replaced by a newer ACTIVE version but retained for historical reports")
public enum TemplateVersionStatus {
    DRAFT,
    ACTIVE,
    SUPERSEDED
}