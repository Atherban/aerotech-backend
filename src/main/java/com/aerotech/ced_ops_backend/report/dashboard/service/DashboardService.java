package com.aerotech.ced_ops_backend.report.dashboard.service;

import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ApprovalSummaryResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.DashboardSummaryResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.MonthlyReportStatisticsResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.RecentActivityResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.RecentReportResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByLineResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByShiftResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsByTypeResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsCreatedTodayResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.ReportsPendingApprovalResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard aggregation over the Generic Report Engine (Phase 4). All queries
 * read exclusively from the module-driven {@code report} table (CompletedReport)
 * and its immutable snapshots — the legacy ReportType tables are no longer
 * consulted. Legacy code and tables are kept intact for coexistence.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String REPORT_TABLE = "report";

    public DashboardSummaryResponse getSummary() {
        Query query = entityManager.createNativeQuery(
                "SELECT status, COUNT(*) FROM " + REPORT_TABLE + " GROUP BY status");
        List<Object[]> rows = query.getResultList();

        long draft = 0, submitted = 0, approved = 0, rejected = 0, total = 0;
        for (Object[] row: rows) {
            String status = (String) row[0];
            long count = ((Number) row[1]).longValue();
            total += count;
            switch (status) {
                case "DRAFT" -> draft = count;
                case "SUBMITTED" -> submitted = count;
                case "APPROVED" -> approved = count;
                case "REJECTED" -> rejected = count;
            }
        }

        return DashboardSummaryResponse.builder()
                .totalReports(total)
                .draftReports(draft)
                .submittedReports(submitted)
                .approvedReports(approved)
                .rejectedReports(rejected)
                .build();
    }

    public List<ReportsByTypeResponse> getReportsByType() {
        Query query = entityManager.createNativeQuery(
                "SELECT module_name, COUNT(*) FROM " + REPORT_TABLE +
                " GROUP BY module_name ORDER BY module_name");
        List<Object[]> rows = query.getResultList();

        List<ReportsByTypeResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            result.add(ReportsByTypeResponse.builder()
                    .reportType((String) row[0])
                    .count(((Number) row[1]).longValue())
                    .build());
        }
        return result;
    }

    public List<ReportsByShiftResponse> getReportsByShift() {
        Query query = entityManager.createNativeQuery(
                "SELECT shift_id, shift_name, COUNT(*) FROM " + REPORT_TABLE +
                " WHERE shift_id IS NOT NULL " +
                "GROUP BY shift_id, shift_name ORDER BY COUNT(*) DESC");
        List<Object[]> rows = query.getResultList();

        List<ReportsByShiftResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            result.add(ReportsByShiftResponse.builder()
                    .shiftId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .shiftName((String) row[1])
                    .count(((Number) row[2]).longValue())
                    .build());
        }
        return result;
    }

    public List<ReportsByLineResponse> getReportsByLine() {
        Query query = entityManager.createNativeQuery(
                "SELECT line_id, line_name, COUNT(*) FROM " + REPORT_TABLE +
                " WHERE line_id IS NOT NULL " +
                "GROUP BY line_id, line_name ORDER BY COUNT(*) DESC");
        List<Object[]> rows = query.getResultList();

        List<ReportsByLineResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            result.add(ReportsByLineResponse.builder()
                    .lineId(row[0] != null ? ((Number) row[0]).longValue() : null)
                    .lineName((String) row[1])
                    .count(((Number) row[2]).longValue())
                    .build());
        }
        return result;
    }

    public ReportsCreatedTodayResponse getReportsCreatedToday() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM " + REPORT_TABLE + " WHERE created_at::date = CURRENT_DATE");
        Number count = (Number) query.getSingleResult();
        return ReportsCreatedTodayResponse.builder()
                .count(count.longValue())
                .build();
    }

    public ReportsPendingApprovalResponse getReportsPendingApproval() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM " + REPORT_TABLE + " WHERE status = 'SUBMITTED'");
        Number count = (Number) query.getSingleResult();
        return ReportsPendingApprovalResponse.builder()
                .count(count.longValue())
                .build();
    }

    public List<RecentReportResponse> getRecentReports() {
        String sql = """
                SELECT r.id, r.report_number, r.module_name, r.started_at::date, r.status,
                       r.shift_name, r.line_name,
                       COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id),
                       r.created_at
                FROM report r
                LEFT JOIN users u ON u.id = r.created_by
                ORDER BY r.created_at DESC
                LIMIT 10
                """;
        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> rows = query.getResultList();

        List<RecentReportResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            Timestamp ts = (Timestamp) row[8];
            result.add(RecentReportResponse.builder()
                    .id(((Number) row[0]).longValue())
                    .reportNumber((String) row[1])
                    .reportType((String) row[2])
                    .reportDate(((Date) row[3]).toLocalDate())
                    .status((String) row[4])
                    .shiftName((String) row[5])
                    .lineName((String) row[6])
                    .createdBy((String) row[7])
                    .createdAt(ts != null ? ts.toLocalDateTime(): null)
                    .build());
        }
        return result;
    }

    public List<MonthlyReportStatisticsResponse> getMonthlyStatistics() {
        Query query = entityManager.createNativeQuery(
                "SELECT EXTRACT(YEAR FROM started_at) as year, EXTRACT(MONTH FROM started_at) as month, " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as approved, " +
                "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected " +
                "FROM " + REPORT_TABLE + " " +
                "GROUP BY EXTRACT(YEAR FROM started_at), EXTRACT(MONTH FROM started_at) " +
                "ORDER BY year DESC, month DESC");
        List<Object[]> rows = query.getResultList();

        List<MonthlyReportStatisticsResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            result.add(MonthlyReportStatisticsResponse.builder()
                    .year(((Number) row[0]).intValue())
                    .month(((Number) row[1]).intValue())
                    .totalReports(((Number) row[2]).longValue())
                    .approvedReports(((Number) row[3]).longValue())
                    .rejectedReports(((Number) row[4]).longValue())
                    .build());
        }
        return result;
    }

    public ApprovalSummaryResponse getApprovalSummary() {
        Query statusQuery = entityManager.createNativeQuery(
                "SELECT status, COUNT(*) FROM " + REPORT_TABLE + " GROUP BY status");
        List<Object[]> statusRows = statusQuery.getResultList();

        long pending = 0, approved = 0, rejected = 0;
        for (Object[] row: statusRows) {
            String status = (String) row[0];
            long count = ((Number) row[1]).longValue();
            switch (status) {
                case "SUBMITTED" -> pending = count;
                case "APPROVED" -> approved = count;
                case "REJECTED" -> rejected = count;
            }
        }

        Query todayQuery = entityManager.createNativeQuery(
                "SELECT status, COUNT(*) FROM " + REPORT_TABLE + " " +
                "WHERE approved_at::date = CURRENT_DATE " +
                "AND status IN ('APPROVED', 'REJECTED') " +
                "GROUP BY status");
        List<Object[]> todayRows = todayQuery.getResultList();

        long approvedToday = 0, rejectedToday = 0;
        for (Object[] row: todayRows) {
            String status = (String) row[0];
            long count = ((Number) row[1]).longValue();
            if ("APPROVED".equals(status)) {
                approvedToday = count;
            } else if ("REJECTED".equals(status)) {
                rejectedToday = count;
            }
        }

        long decided = approved + rejected;
        double approvalRate = decided > 0
                ? Math.round((approved * 10000.0) / decided) / 100.0
                : 0.0;

        return ApprovalSummaryResponse.builder()
                .pendingApprovals(pending)
                .approvedReports(approved)
                .rejectedReports(rejected)
                .approvedToday(approvedToday)
                .rejectedToday(rejectedToday)
                .approvalRate(approvalRate)
                .build();
    }

    public List<RecentActivityResponse> getRecentActivity(int limit) {
        String sql = """
                SELECT r.id, r.report_number, r.module_name, r.action, r.status,
                       COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id) as actor,
                       r.event_time
                FROM (
                    SELECT id, report_number, module_name, 'CREATED' as action,
                           status, created_by as actor_id, created_at as event_time
                    FROM report
                    UNION ALL
                    SELECT id, report_number, module_name,
                           CASE WHEN status = 'APPROVED' THEN 'APPROVED' ELSE 'REJECTED' END,
                           status, approved_by, approved_at
                    FROM report
                    WHERE approved_at IS NOT NULL
                ) r
                LEFT JOIN users u ON u.id = r.actor_id
                ORDER BY r.event_time DESC
                LIMIT :limit
                """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("limit", limit);
        List<Object[]> rows = query.getResultList();

        List<RecentActivityResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            Timestamp ts = (Timestamp) row[6];
            result.add(RecentActivityResponse.builder()
                    .id(((Number) row[0]).longValue())
                    .reportNumber((String) row[1])
                    .reportType((String) row[2])
                    .action((String) row[3])
                    .status((String) row[4])
                    .actor((String) row[5])
                    .timestamp(ts != null ? ts.toLocalDateTime() : null)
                    .build());
        }
        return result;
    }

}
