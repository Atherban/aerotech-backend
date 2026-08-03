package com.aerotech.ced_ops_backend.report.support;

import com.aerotech.ced_ops_backend.common.enums.ReportType;

import java.util.Arrays;

/**
 * Lightweight, declarative metadata for each predefined {@link ReportType}.
 *
 * <p>This registry intentionally holds <b>no Java class references</b> - it only
 * carries display/label and naming/permission metadata used by the shared report
 * engine. Report-specific behaviour remains in the thin per-report service,
 * mapper and repository classes.
 */
public enum ReportTypeMetadata {

    PROCESS_MONITORING(
            ReportType.PROCESS_MONITORING,
            "Process monitoring",
            "PMR",
            "SUPER_ADMIN,ADMIN",
            "SUPER_ADMIN",
            "process-monitoring"
    ),
    PDI(
            ReportType.PDI,
            "Pre-delivery inspection",
            "PDI",
            "SUPER_ADMIN,ADMIN",
            "SUPER_ADMIN",
            "pre-delivery-inspection"
    ),
    DAILY_STARTUP(
            ReportType.DAILY_STARTUP,
            "Daily startup",
            "DSR",
            "SUPER_ADMIN,ADMIN",
            "SUPER_ADMIN",
            "daily-startup"
    ),
    CHEMICAL_CONSUMPTION(
            ReportType.CHEMICAL_CONSUMPTION,
            "Chemical consumption",
            "CCR",
            "SUPER_ADMIN,ADMIN",
            "SUPER_ADMIN",
            "chemical-consumption"
    ),
    FIRST_PIECE_INSPECTION(
            ReportType.FIRST_PIECE_INSPECTION,
            "First piece inspection",
            "FPI",
            "SUPER_ADMIN,ADMIN",
            "SUPER_ADMIN",
            "first-piece-inspection"
    ),
    DAILY_INSPECTION(
            ReportType.DAILY_INSPECTION,
            "Daily inspection",
            "DIR",
            "SUPER_ADMIN,ADMIN",
            "SUPER_ADMIN",
            "daily-inspection"
    );

    private final ReportType reportType;
    private final String label;
    private final String prefix;
    private final String approveRoles;
    private final String deleteRole;
    private final String template;

    ReportTypeMetadata(
            ReportType reportType,
            String label,
            String prefix,
            String approveRoles,
            String deleteRole,
            String template
    ) {
        this.reportType = reportType;
        this.label = label;
        this.prefix = prefix;
        this.approveRoles = approveRoles;
        this.deleteRole = deleteRole;
        this.template = template;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getLabel() {
        return label;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getApproveRoles() {
        return approveRoles;
    }

    public String getDeleteRole() {
        return deleteRole;
    }

    public String getTemplate() {
        return template;
    }

    public static ReportTypeMetadata of(ReportType reportType) {
        return Arrays.stream(values())
                .filter(meta -> meta.reportType == reportType)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unsupported report type: " + reportType));
    }
}