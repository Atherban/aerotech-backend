package com.aerotech.ced_ops_backend.report.dashboard.service;

import com.aerotech.ced_ops_backend.report.dashboard.dto.response.DashboardSummaryResponse;
import com.aerotech.ced_ops_backend.report.dashboard.dto.response.MonthlyReportStatisticsResponse;
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

import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String STATUS_UNION =
            "SELECT status FROM process_monitoring_reports UNION ALL " +
            "SELECT status FROM chemical_consumption_reports UNION ALL " +
            "SELECT status FROM daily_startup_reports UNION ALL " +
            "SELECT status FROM first_piece_inspection_reports UNION ALL " +
            "SELECT status FROM daily_inspection_reports UNION ALL " +
            "SELECT status FROM pre_delivery_inspection_reports";

    private static final String SHIFT_UNION =
            "SELECT shift_id FROM process_monitoring_reports UNION ALL " +
            "SELECT shift_id FROM chemical_consumption_reports UNION ALL " +
            "SELECT shift_id FROM daily_startup_reports UNION ALL " +
            "SELECT shift_id FROM first_piece_inspection_reports UNION ALL " +
            "SELECT shift_id FROM daily_inspection_reports UNION ALL " +
            "SELECT shift_id FROM pre_delivery_inspection_reports";

    private static final String LINE_UNION =
            "SELECT line_id FROM process_monitoring_reports UNION ALL " +
            "SELECT line_id FROM chemical_consumption_reports UNION ALL " +
            "SELECT line_id FROM daily_startup_reports UNION ALL " +
            "SELECT line_id FROM first_piece_inspection_reports UNION ALL " +
            "SELECT line_id FROM daily_inspection_reports UNION ALL " +
            "SELECT line_id FROM pre_delivery_inspection_reports";

    private static final String DATE_STATUS_UNION =
            "SELECT report_date, status FROM process_monitoring_reports UNION ALL " +
            "SELECT report_date, status FROM chemical_consumption_reports UNION ALL " +
            "SELECT report_date, status FROM daily_startup_reports UNION ALL " +
            "SELECT report_date, status FROM first_piece_inspection_reports UNION ALL " +
            "SELECT report_date, status FROM daily_inspection_reports UNION ALL " +
            "SELECT report_date, status FROM pre_delivery_inspection_reports";

    private static final String RECENT_UNION =
            "SELECT id, report_number, 'PROCESS_MONITORING' as report_type, report_date, status, shift_id, line_id, created_by, created_at " +
            "FROM process_monitoring_reports UNION ALL " +
            "SELECT id, report_number, 'CHEMICAL_CONSUMPTION', report_date, status, shift_id, line_id, created_by, created_at " +
            "FROM chemical_consumption_reports UNION ALL " +
            "SELECT id, report_number, 'DAILY_STARTUP', report_date, status, shift_id, line_id, created_by, created_at " +
            "FROM daily_startup_reports UNION ALL " +
            "SELECT id, report_number, 'FIRST_PIECE_INSPECTION', report_date, status, shift_id, line_id, created_by, created_at " +
            "FROM first_piece_inspection_reports UNION ALL " +
            "SELECT id, report_number, 'DAILY_INSPECTION', report_date, status, shift_id, line_id, created_by, created_at " +
            "FROM daily_inspection_reports UNION ALL " +
            "SELECT id, report_number, 'PDI', report_date, status, shift_id, line_id, created_by, created_at " +
            "FROM pre_delivery_inspection_reports";

    private static final String TYPE_UNION =
            "SELECT 'PROCESS_MONITORING' as report_type FROM process_monitoring_reports UNION ALL " +
            "SELECT 'CHEMICAL_CONSUMPTION' FROM chemical_consumption_reports UNION ALL " +
            "SELECT 'DAILY_STARTUP' FROM daily_startup_reports UNION ALL " +
            "SELECT 'FIRST_PIECE_INSPECTION' FROM first_piece_inspection_reports UNION ALL " +
            "SELECT 'DAILY_INSPECTION' FROM daily_inspection_reports UNION ALL " +
            "SELECT 'PDI' FROM pre_delivery_inspection_reports";

    public DashboardSummaryResponse getSummary() {
        Query query = entityManager.createNativeQuery(
                "SELECT status, COUNT(*) as count FROM (" + STATUS_UNION + ") combined GROUP BY status");
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
                "SELECT report_type, COUNT(*) as count FROM (" + TYPE_UNION + ") combined " +
                "GROUP BY report_type ORDER BY report_type");
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
                "SELECT s.id, s.name, COUNT(*) as count " +
                "FROM (" + SHIFT_UNION + ") combined " +
                "JOIN shifts s ON s.id = combined.shift_id " +
                "GROUP BY s.id, s.name ORDER BY count DESC");
        List<Object[]> rows = query.getResultList();

        List<ReportsByShiftResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            result.add(ReportsByShiftResponse.builder()
                    .shiftId(((Number) row[0]).longValue())
                    .shiftName((String) row[1])
                    .count(((Number) row[2]).longValue())
                    .build());
        }
        return result;
    }

    public List<ReportsByLineResponse> getReportsByLine() {
        Query query = entityManager.createNativeQuery(
                "SELECT l.id, l.name, COUNT(*) as count " +
                "FROM (" + LINE_UNION + ") combined " +
                "JOIN line_master l ON l.id = combined.line_id " +
                "GROUP BY l.id, l.name ORDER BY count DESC");
        List<Object[]> rows = query.getResultList();

        List<ReportsByLineResponse> result = new ArrayList<>();
        for (Object[] row: rows) {
            result.add(ReportsByLineResponse.builder()
                    .lineId(((Number) row[0]).longValue())
                    .lineName((String) row[1])
                    .count(((Number) row[2]).longValue())
                    .build());
        }
        return result;
    }

    public ReportsCreatedTodayResponse getReportsCreatedToday() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM (" +
                "SELECT id FROM process_monitoring_reports WHERE created_at::date = CURRENT_DATE UNION ALL " +
                "SELECT id FROM chemical_consumption_reports WHERE created_at::date = CURRENT_DATE UNION ALL " +
                "SELECT id FROM daily_startup_reports WHERE created_at::date = CURRENT_DATE UNION ALL " +
                "SELECT id FROM first_piece_inspection_reports WHERE created_at::date = CURRENT_DATE UNION ALL " +
                "SELECT id FROM daily_inspection_reports WHERE created_at::date = CURRENT_DATE UNION ALL " +
                "SELECT id FROM pre_delivery_inspection_reports WHERE created_at::date = CURRENT_DATE" +
                ") combined");
        Number count = (Number) query.getSingleResult();
        return ReportsCreatedTodayResponse.builder()
                .count(count.longValue())
                .build();
    }

    public ReportsPendingApprovalResponse getReportsPendingApproval() {
        Query query = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM (" + STATUS_UNION + ") combined WHERE status = 'SUBMITTED'");
        Number count = (Number) query.getSingleResult();
        return ReportsPendingApprovalResponse.builder()
                .count(count.longValue())
                .build();
    }

    public List<RecentReportResponse> getRecentReports() {
        String sql = """
                SELECT r.id, r.report_number, r.report_type, r.report_date, r.status,
                       s.name as shift_name, l.name as line_name,
                       COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id) as created_by,
                       r.created_at
                FROM (
                """ + RECENT_UNION + """
                ) r
                LEFT JOIN shifts s ON s.id = r.shift_id
                LEFT JOIN line_master l ON l.id = r.line_id
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
                "SELECT EXTRACT(YEAR FROM report_date) as year, EXTRACT(MONTH FROM report_date) as month, " +
                "COUNT(*) as total, " +
                "SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) as approved, " +
                "SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) as rejected " +
                "FROM (" + DATE_STATUS_UNION + ") combined " +
                "GROUP BY EXTRACT(YEAR FROM report_date), EXTRACT(MONTH FROM report_date) " +
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

}
