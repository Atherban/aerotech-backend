package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Status of a report through its lifecycle. Values: DRAFT - Report has been saved as draft and not yet submitted; SUBMITTED - Report has been submitted for approval; APPROVED - Report has been approved; REJECTED - Report has been rejected")
public enum ReportStatus {
    DRAFT, // Report has been saved as draft and not yet submitted
    SUBMITTED, // Report has been submitted for approval
    APPROVED, // Report has been approved
    REJECTED // Report has been rejected
}
