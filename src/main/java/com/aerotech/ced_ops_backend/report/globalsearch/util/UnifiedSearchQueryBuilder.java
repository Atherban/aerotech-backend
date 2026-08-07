package com.aerotech.ced_ops_backend.report.globalsearch.util;

import com.aerotech.ced_ops_backend.report.globalsearch.dto.request.UnifiedSearchRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds the native SQL for the unified enterprise search (reports + users +
 * parameters) over a single UNION of the three entity sources, plus the count
 * query. All filters are optional and parameterized.
 *
 * <p>Phase 4: the REPORT branch now reads exclusively from the Generic Report
 * Engine {@code report} table (CompletedReport) and its immutable snapshots,
 * instead of the legacy ReportType tables. Report results carry the module
 * identity (module_name / module_type), shift, line, date and status from the
 * engine; the PARAMETER branch reads the module architecture's global
 * {@code parameter} table, and the user branch reads {@code users}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UnifiedSearchQueryBuilder {

    private static final Map<String, String> SORT_COLUMN_MAP = Map.of(
            "createdAt", "created_at",
            "reportDate", "report_date",
            "title", "title",
            "reportType", "report_type",
            "status", "status"
    );

    private static final String REPORT_SELECT = """
            SELECT 'REPORT' AS type, r.id, r.report_number AS title,
                   r.module_name AS subtitle,
                   r.module_name AS report_type,
                   r.status::varchar AS status,
                   r.shift_name AS shift_name, r.line_name AS line_name,
                   COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id) AS actor,
                   r.started_at::date AS report_date, r.created_at,
                   NULL::varchar AS role_name, r.shift_id, r.line_id,
                   r.module_id, r.module_type_id
            FROM report r
            LEFT JOIN users u ON u.id = r.created_by
            """;

    private static final String USER_SELECT = """
            SELECT 'USER' AS type, u.id, u.employee_id AS title,
                   CONCAT(u.first_name, ' ', u.last_name) AS subtitle,
                   NULL::varchar AS report_type, NULL::varchar AS status,
                   NULL::varchar AS shift_name, NULL::varchar AS line_name,
                   CONCAT(u.first_name, ' ', u.last_name) AS actor,
                   NULL::date AS report_date, u.created_at,
                   r.name AS role_name, NULL::bigint AS shift_id, NULL::bigint AS line_id,
                   NULL::bigint AS module_id, NULL::bigint AS module_type_id
            FROM users u
            LEFT JOIN roles r ON r.id = u.role_id
            """;

    private static final String PARAMETER_SELECT = """
            SELECT 'PARAMETER' AS type, p.id, p.name AS title,
                   COALESCE(p.description, '') AS subtitle,
                   NULL::varchar AS report_type,
                   NULL::varchar AS status,
                   NULL::varchar AS shift_name, NULL::varchar AS line_name,
                   NULL::varchar AS actor,
                   NULL::date AS report_date, p.created_at,
                   NULL::varchar AS role_name, NULL::bigint AS shift_id, NULL::bigint AS line_id,
                   NULL::bigint AS module_id, NULL::bigint AS module_type_id
            FROM parameter p
            """;

    private static final String COMMON_SELECT =
            "SELECT type, id, title, subtitle, report_type, status, " +
            "shift_name, line_name, actor, report_date, created_at, " +
            "role_name, shift_id, line_id, module_id, module_type_id FROM (";

    public static String buildDataQuery(UnifiedSearchRequest request) {
        return COMMON_SELECT
                + unionBody(request)
                + ") src WHERE 1=1"
                + buildWhereClause(request)
                + buildOrderBy(request)
                + " LIMIT :size OFFSET :offset";
    }

    public static String buildCountQuery(UnifiedSearchRequest request) {
        return "SELECT COUNT(*) FROM (" + unionBody(request) + ") src WHERE 1=1"
                + buildWhereClause(request);
    }

    private static String unionBody(UnifiedSearchRequest request) {
        List<String> branches = new ArrayList<>();
        String type = request == null ? null : request.getType();

        if (type == null || UnifiedSearchRequest.TYPE_REPORT.equalsIgnoreCase(type)) {
            branches.add(REPORT_SELECT);
        }
        if (type == null || UnifiedSearchRequest.TYPE_USER.equalsIgnoreCase(type)) {
            branches.add(USER_SELECT);
        }
        if (type == null || UnifiedSearchRequest.TYPE_PARAMETER.equalsIgnoreCase(type)) {
            branches.add(PARAMETER_SELECT);
        }
        return String.join(" UNION ALL ", branches);
    }

    private static String buildWhereClause(UnifiedSearchRequest request) {
        if (request == null) {
            return "";
        }
        List<String> conditions = new ArrayList<>();

        if (isPresent(request.getKeyword())) {
            conditions.add("(title ILIKE '%' || :keyword || '%'"
                    + " OR subtitle ILIKE '%' || :keyword || '%'"
                    + " OR COALESCE(actor, '') ILIKE '%' || :keyword || '%'"
                    + " OR COALESCE(report_type, '') ILIKE '%' || :keyword || '%')");
        }
        if (isPresent(request.getReportNumber())) {
            conditions.add("title ILIKE '%' || :reportNumber || '%'");
        }
        if (isPresent(request.getReportType())) {
            conditions.add("report_type = :reportType");
        }
        if (isPresent(request.getStatus())) {
            conditions.add("status = :status");
        }
        if (isPresent(request.getEmployeeName())) {
            conditions.add("COALESCE(actor, '') ILIKE '%' || :employeeName || '%'");
        }
        if (isPresent(request.getRole())) {
            conditions.add("role_name = :role");
        }
        if (request.getShiftId() != null) {
            conditions.add("shift_id = :shiftId");
        }
        if (request.getLineId() != null) {
            conditions.add("line_id = :lineId");
        }
        if (request.getModuleId() != null) {
            conditions.add("module_id = :moduleId");
        }
        if (request.getModuleTypeId() != null) {
            conditions.add("module_type_id = :moduleTypeId");
        }
        if (request.getDateFrom() != null) {
            conditions.add("report_date >= :dateFrom");
        }
        if (request.getDateTo() != null) {
            conditions.add("report_date <= :dateTo");
        }

        return conditions.isEmpty() ? "" : " AND " + String.join(" AND ", conditions);
    }

    private static String buildOrderBy(UnifiedSearchRequest request) {
        String sortBy = request == null ? null : request.getSortBy();
        if (sortBy == null || sortBy.isBlank() || !SORT_COLUMN_MAP.containsKey(sortBy)) {
            return " ORDER BY created_at DESC";
        }
        String column = SORT_COLUMN_MAP.get(sortBy);
        String dir = "DESC";
        if ("ASC".equalsIgnoreCase(request.getSortDirection())) {
            dir = "ASC";
        }
        return " ORDER BY " + column + " " + dir;
    }

    public static Map<String, Object> extractParams(UnifiedSearchRequest request) {
        Map<String, Object> params = new HashMap<>();
        if (request == null) {
            return params;
        }
        putIfPresent(params, "keyword", request.getKeyword());
        putIfPresent(params, "reportNumber", request.getReportNumber());
        putIfPresent(params, "reportType", request.getReportType());
        putIfPresent(params, "status", request.getStatus());
        putIfPresent(params, "employeeName", request.getEmployeeName());
        putIfPresent(params, "role", request.getRole());
        if (request.getShiftId() != null) params.put("shiftId", request.getShiftId());
        if (request.getLineId() != null) params.put("lineId", request.getLineId());
        if (request.getModuleId() != null) params.put("moduleId", request.getModuleId());
        if (request.getModuleTypeId() != null) params.put("moduleTypeId", request.getModuleTypeId());
        if (request.getDateFrom() != null) params.put("dateFrom", request.getDateFrom());
        if (request.getDateTo() != null) params.put("dateTo", request.getDateTo());
        return params;
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }

    private static void putIfPresent(Map<String, Object> map, String key, String value) {
        if (isPresent(value)) {
            map.put(key, value);
        }
    }

}