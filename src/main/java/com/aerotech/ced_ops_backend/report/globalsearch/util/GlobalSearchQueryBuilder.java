package com.aerotech.ced_ops_backend.report.globalsearch.util;

import com.aerotech.ced_ops_backend.report.globalsearch.dto.request.GlobalSearchRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalSearchQueryBuilder {

    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "reportDate", "r.report_date",
            "reportNumber", "r.report_number",
            "createdAt", "r.created_at",
            "updatedAt", "r.updated_at",
            "status", "r.status"
    );

    private static final String BASE_SELECT =
            "SELECT r.id, r.report_number, r.report_type, r.report_date, r.status, " +
            "s.name AS shift_name, l.name AS line_name, " +
            "COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id) AS created_by_name, " +
            "COALESCE(CONCAT(ua.first_name, ' ', ua.last_name), ua.employee_id) AS approved_by_name, " +
            "COALESCE(r.remarks, '') AS summary " +
            "FROM (" +
            "SELECT id, report_number, 'PROCESS_MONITORING'::varchar AS report_type, report_date, status::varchar, shift_id, line_id, created_by, approved_by, remarks::varchar, NULL::bigint, created_at, updated_at FROM process_monitoring_reports " +
            "UNION ALL " +
            "SELECT id, report_number, 'CHEMICAL_CONSUMPTION'::varchar, report_date, status::varchar, shift_id, line_id, created_by, approved_by, remarks::varchar, NULL::bigint, created_at, updated_at FROM chemical_consumption_reports " +
            "UNION ALL " +
            "SELECT id, report_number, 'DAILY_STARTUP'::varchar, report_date, status::varchar, shift_id, line_id, created_by, approved_by, remarks::varchar, NULL::bigint, created_at, updated_at FROM daily_startup_reports " +
            "UNION ALL " +
            "SELECT id, report_number, 'FIRST_PIECE_INSPECTION'::varchar, report_date, status::varchar, shift_id, line_id, created_by, approved_by, remarks::varchar, NULL::bigint, created_at, updated_at FROM first_piece_inspection_reports " +
            "UNION ALL " +
            "SELECT id, report_number, 'DAILY_INSPECTION'::varchar, report_date, status::varchar, shift_id, line_id, created_by, approved_by, remarks::varchar, NULL::bigint, created_at, updated_at FROM daily_inspection_reports " +
            "UNION ALL " +
            "SELECT id, report_number, 'PDI'::varchar, report_date, status::varchar, shift_id, line_id, created_by, approved_by, remarks::varchar, NULL::bigint, created_at, updated_at FROM pre_delivery_inspection_reports " +
            ") r " +
            "LEFT JOIN shifts s ON s.id = r.shift_id " +
            "LEFT JOIN line_master l ON l.id = r.line_id " +
            "LEFT JOIN users u ON u.id = r.created_by " +
            "LEFT JOIN users ua ON ua.id = r.approved_by";

    private static final String COUNT_SELECT = "SELECT COUNT(*) " +
            "FROM (" +
            "SELECT id, report_number, report_type, report_date, status, shift_id, line_id, created_by, approved_by, remarks FROM process_monitoring_reports " +
            "UNION ALL SELECT id, report_number, report_type, report_date, status, shift_id, line_id, created_by, approved_by, remarks FROM chemical_consumption_reports " +
            "UNION ALL SELECT id, report_number, report_type, report_date, status, shift_id, line_id, created_by, approved_by, remarks FROM daily_startup_reports " +
            "UNION ALL SELECT id, report_number, report_type, report_date, status, shift_id, line_id, created_by, approved_by, remarks FROM first_piece_inspection_reports " +
            "UNION ALL SELECT id, report_number, report_type, report_date, status, shift_id, line_id, created_by, approved_by, remarks FROM daily_inspection_reports " +
            "UNION ALL SELECT id, report_number, report_type, report_date, status, shift_id, line_id, created_by, approved_by, remarks FROM pre_delivery_inspection_reports " +
            ") r " +
            "LEFT JOIN shifts s ON s.id = r.shift_id " +
            "LEFT JOIN line_master l ON l.id = r.line_id " +
            "LEFT JOIN users u ON u.id = r.created_by " +
            "LEFT JOIN users ua ON ua.id = r.approved_by";

    public static String buildDataQuery() {
        return BASE_SELECT + buildWhereClause(null) + buildOrderBy(null, null);
    }

    public static String buildDataQuery(GlobalSearchRequest request) {
        return BASE_SELECT + buildWhereClause(request) + buildOrderBy(
                request != null ? request.getSortBy() : null,
                request != null ? request.getSortDirection() : null
        );
    }

    public static String buildCountQuery() {
        return COUNT_SELECT + buildWhereClause(null);
    }

    public static String buildCountQuery(GlobalSearchRequest request) {
        return COUNT_SELECT + buildWhereClause(request);
    }

    public static Map<String, Object> extractParams(GlobalSearchRequest request) {
        Map<String, Object> params = new HashMap<>();
        if (request == null) {
            return params;
        }
        putIfPresent(params, "reportNumber", request.getReportNumber());
        putIfPresent(params, "reportType", request.getReportType());
        putIfPresent(params, "status", request.getStatus());
        putIfPresent(params, "employeeName", request.getEmployeeName());
        putIfPresent(params, "employeeId", request.getEmployeeId());
        putIfPresent(params, "shiftId", request.getShiftId());
        putIfPresent(params, "lineId", request.getLineId());
        putIfPresent(params, "dateFrom", request.getDateFrom());
        putIfPresent(params, "dateTo", request.getDateTo());
        putIfPresent(params, "remarks", request.getRemarks());
        putIfPresent(params, "keyword", request.getKeyword());
        putIfPresent(params, "createdBy", request.getCreatedBy());
        putIfPresent(params, "approvedBy", request.getApprovedBy());
        return params;
    }

    private static String buildWhereClause(GlobalSearchRequest request) {
        List<String> conditions = new ArrayList<>();
        conditions.add("1=1");

        if (request == null) {
            return " WHERE " + String.join(" AND ", conditions);
        }

        if (isPresent(request.getReportNumber())) {
            conditions.add("r.report_number ILIKE '%' || :reportNumber || '%'");
        }
        if (isPresent(request.getReportType())) {
            conditions.add("r.report_type = :reportType");
        }
        if (isPresent(request.getStatus())) {
            conditions.add("r.status = :status");
        }
        if (isPresent(request.getEmployeeName())) {
            conditions.add("CONCAT(u.first_name, ' ', u.last_name) ILIKE '%' || :employeeName || '%'");
        }
        if (isPresent(request.getEmployeeId())) {
            conditions.add("u.employee_id = :employeeId");
        }
        if (request.getShiftId() != null) {
            conditions.add("r.shift_id = :shiftId");
        }
        if (request.getLineId() != null) {
            conditions.add("r.line_id = :lineId");
        }
        if (request.getDateFrom() != null) {
            conditions.add("r.report_date >= :dateFrom");
        }
        if (request.getDateTo() != null) {
            conditions.add("r.report_date <= :dateTo");
        }
        if (isPresent(request.getRemarks())) {
            conditions.add("r.remarks ILIKE '%' || :remarks || '%'");
        }
        if (request.getApproved() != null) {
            if (request.getApproved()) {
                conditions.add("r.status = 'APPROVED'");
            } else {
                conditions.add("r.status IN ('DRAFT', 'SUBMITTED', 'REJECTED')");
            }
        }
        if (isPresent(request.getKeyword())) {
            conditions.add("(r.report_number ILIKE '%' || :keyword || '%' " +
                    "OR r.remarks ILIKE '%' || :keyword || '%' " +
                    "OR CONCAT(u.first_name, ' ', u.last_name) ILIKE '%' || :keyword || '%')");
        }
        if (isPresent(request.getCreatedBy())) {
            conditions.add("u.employee_id = :createdBy");
        }
        if (isPresent(request.getApprovedBy())) {
            conditions.add("ua.employee_id = :approvedBy");
        }

        return " WHERE " + String.join(" AND ", conditions);
    }

    private static String buildOrderBy(String sortBy, String sortDirection) {
        if (sortBy == null || sortBy.isBlank() || !SORT_COLUMN_MAP.containsKey(sortBy)) {
            return " ORDER BY r.created_at DESC";
        }
        String column = SORT_COLUMN_MAP.get(sortBy);
        String dir = "DESC";
        if ("ASC".equalsIgnoreCase(sortDirection)) {
            dir = "ASC";
        }
        return " ORDER BY " + column + " " + dir;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static void putIfPresent(Map<String, Object> map, String key, Object value) {
        if (value instanceof String s) {
            if (!s.isBlank()) {
                map.put(key, value);
            }
        } else if (value != null) {
            map.put(key, value);
        }
    }

}
