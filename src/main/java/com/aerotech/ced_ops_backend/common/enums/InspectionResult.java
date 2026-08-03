package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of an inspection check. Values: PASS - Inspection passed; FAIL - Inspection failed; NOT_APPLICABLE - Inspection check is not applicable")
public enum InspectionResult {

    PASS, // Inspection passed
    FAIL, // Inspection failed
    NOT_APPLICABLE // Inspection check is not applicable

}