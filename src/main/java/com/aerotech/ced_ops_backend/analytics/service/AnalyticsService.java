package com.aerotech.ced_ops_backend.analytics.service;

import com.aerotech.ced_ops_backend.analytics.dto.response.ChartDataPoint;
import com.aerotech.ced_ops_backend.analytics.dto.response.ChemicalConsumptionKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.KPICard;
import com.aerotech.ced_ops_backend.analytics.dto.response.LinePerformanceResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.OperatorPerformanceResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ProcessMonitoringKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ProductivityKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.QualityKPIResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ReportOverviewResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.ShiftPerformanceResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.TimeAnalyticsResponse;
import com.aerotech.ced_ops_backend.analytics.dto.response.TrendPoint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String REPORT_UNION =
            "SELECT id, report_number, report_type, report_date, status, shift_id, line_id, " +
            "created_by, approved_by, created_at, approved_at " +
            "FROM process_monitoring_reports UNION ALL " +
            "SELECT id, report_number, report_type, report_date, status, shift_id, line_id, " +
            "created_by, approved_by, created_at, approved_at " +
            "FROM chemical_consumption_reports UNION ALL " +
            "SELECT id, report_number, report_type, report_date, status, shift_id, line_id, " +
            "created_by, approved_by, created_at, approved_at " +
            "FROM daily_startup_reports UNION ALL " +
            "SELECT id, report_number, report_type, report_date, status, shift_id, line_id, " +
            "created_by, approved_by, created_at, approved_at " +
            "FROM first_piece_inspection_reports UNION ALL " +
            "SELECT id, report_number, report_type, report_date, status, shift_id, line_id, " +
            "created_by, approved_by, created_at, approved_at " +
            "FROM daily_inspection_reports UNION ALL " +
            "SELECT id, report_number, report_type, report_date, status, shift_id, line_id, " +
            "created_by, approved_by, created_at, approved_at " +
            "FROM pre_delivery_inspection_reports";

    public ReportOverviewResponse getReportOverview(LocalDate dateFrom, LocalDate dateTo,
                                                    Long shiftId, Long lineId) {
        String base = REPORT_UNION;
        String where = buildWhereClause(dateFrom, dateTo, shiftId, lineId);
        String from = "FROM (" + base + ") r" + where;

        long total = count(from, dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byType = aggregateByColumn(from, "r.report_type", dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byStatus = aggregateByColumn(from, "r.status", dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byShift = aggregateWithJoin(from, "r.shift_id", "shifts", "name", dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byLine = aggregateWithJoin(from, "r.line_id", "line_master", "name", dateFrom, dateTo, shiftId, lineId);

        return ReportOverviewResponse.builder()
                .totalReports(total)
                .reportsByType(byType)
                .reportsByStatus(byStatus)
                .reportsByShift(byShift)
                .reportsByLine(byLine)
                .build();
    }

    public QualityKPIResponse getQualityKPIs(LocalDate dateFrom, LocalDate dateTo,
                                             Long shiftId, Long lineId) {
        String base = REPORT_UNION;
        String where = buildWhereClause(dateFrom, dateTo, shiftId, lineId);
        String from = "FROM (" + base + ") r" + where;

        long total = count(from, dateFrom, dateTo, shiftId, lineId);
        long approved = countWithCondition(from, "r.status = 'APPROVED'", dateFrom, dateTo, shiftId, lineId);
        long rejected = countWithCondition(from, "r.status = 'REJECTED'", dateFrom, dateTo, shiftId, lineId);
        long submitted = countWithCondition(from, "r.status = 'SUBMITTED'", dateFrom, dateTo, shiftId, lineId);

        double approvalRate = total > 0 ? (double) approved / total * 100 : 0;
        double rejectionRate = total > 0 ? (double) rejected / total * 100 : 0;

        String entryUnion = buildEntryUnion();
        String entryWhere = buildEntryWhere(dateFrom, dateTo, shiftId, lineId);
        String entryFrom = "FROM (" + entryUnion + ") e" + entryWhere;

        long passCount = countWithCondition(entryFrom, "e.result = 'PASS'", dateFrom, dateTo, shiftId, lineId);
        long failCount = countWithCondition(entryFrom, "e.result = 'FAIL'", dateFrom, dateTo, shiftId, lineId);
        long totalEntries = passCount + failCount + countWithCondition(entryFrom, "e.result = 'NOT_APPLICABLE'", dateFrom, dateTo, shiftId, lineId);
        double passRate = totalEntries > 0 ? (double) passCount / totalEntries * 100 : 0;
        double failRate = totalEntries > 0 ? (double) failCount / totalEntries * 100 : 0;

        String trendSql = "SELECT r.report_date, COUNT(*) FROM (" + base + ") r" +
                (dateFrom != null || dateTo != null ? " WHERE " : "") +
                (dateFrom != null ? "r.report_date >= :dateFrom" : "") +
                (dateFrom != null && dateTo != null ? " AND " : "") +
                (dateTo != null ? "r.report_date <= :dateTo" : "") +
                " GROUP BY r.report_date ORDER BY r.report_date";

        List<TrendPoint> dailyTrend = executeTrendQuery(trendSql, dateFrom, dateTo, shiftId, lineId);

        List<ChartDataPoint> passFailByType = getPassFailByType(dateFrom, dateTo, shiftId, lineId);

        List<KPICard> kpis = List.of(
                KPICard.builder().label("Approval Rate").value(String.format("%.1f", approvalRate)).unit("%").build(),
                KPICard.builder().label("Rejection Rate").value(String.format("%.1f", rejectionRate)).unit("%").build(),
                KPICard.builder().label("Pass Rate").value(String.format("%.1f", passRate)).unit("%").build(),
                KPICard.builder().label("Fail Rate").value(String.format("%.1f", failRate)).unit("%").build(),
                KPICard.builder().label("Total Reports").value(String.valueOf(total)).unit("").build(),
                KPICard.builder().label("Pending Approval").value(String.valueOf(submitted)).unit("").build()
        );

        return QualityKPIResponse.builder()
                .kpiCards(kpis)
                .dailyInspectionTrend(dailyTrend)
                .passFailByType(passFailByType)
                .build();
    }

    public ChemicalConsumptionKPIResponse getChemicalConsumptionKPIs(LocalDate dateFrom,
                                                                     LocalDate dateTo, Long lineId) {
        String base = "SELECT id, report_date, shift_id, line_id, created_by " +
                "FROM chemical_consumption_reports";
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (dateFrom != null) where.append(" AND report_date >= :dateFrom");
        if (dateTo != null) where.append(" AND report_date <= :dateTo");
        if (lineId != null) where.append(" AND line_id = :lineId");
        String from = "FROM (" + base + where + ") r";

        long total = count(from, dateFrom, dateTo, null, lineId);
        long dailyCount = countWithDateRange(
                "SELECT id, report_date FROM chemical_consumption_reports", null, null);

        List<TrendPoint> dailyTrend = executeTrendQuery(
                "SELECT report_date, COUNT(*) FROM chemical_consumption_reports" + where +
                " GROUP BY report_date ORDER BY report_date", dateFrom, dateTo, null, lineId);

        List<TrendPoint> weeklyTrend = executeTrendQuery(
                "SELECT DATE_TRUNC('week', report_date)::date, COUNT(*) " +
                "FROM chemical_consumption_reports" + where +
                " GROUP BY DATE_TRUNC('week', report_date) ORDER BY 1", dateFrom, dateTo, null, lineId);

        List<TrendPoint> monthlyTrend = executeTrendQuery(
                "SELECT DATE_TRUNC('month', report_date)::date, COUNT(*) " +
                "FROM chemical_consumption_reports" + where +
                " GROUP BY DATE_TRUNC('month', report_date) ORDER BY 1", dateFrom, dateTo, null, lineId);

        String lineSql = "SELECT l.name, COUNT(*) FROM chemical_consumption_reports r " +
                "JOIN line_master l ON l.id = r.line_id" + where +
                " GROUP BY l.name ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> byLine = executeChartQuery(lineSql, dateFrom, dateTo, null, lineId);

        List<KPICard> kpis = List.of(
                KPICard.builder().label("Total Chemical Reports").value(String.valueOf(total)).unit("").build(),
                KPICard.builder().label("Today's Reports").value(String.valueOf(dailyCount)).unit("").build()
        );

        return ChemicalConsumptionKPIResponse.builder()
                .kpiCards(kpis)
                .dailyTrend(dailyTrend)
                .weeklyTrend(weeklyTrend)
                .monthlyTrend(monthlyTrend)
                .consumptionByLine(byLine)
                .build();
    }

    public ProcessMonitoringKPIResponse getProcessMonitoringKPIs(LocalDate dateFrom,
                                                                  LocalDate dateTo, Long lineId) {
        StringBuilder where = new StringBuilder(" WHERE 1=1");
        if (dateFrom != null) where.append(" AND r.report_date >= :dateFrom");
        if (dateTo != null) where.append(" AND r.report_date <= :dateTo");
        if (lineId != null) where.append(" AND r.line_id = :lineId");

        String entrySql = "SELECT e.inspection_result as result, p.parameter_name, COUNT(*) as cnt " +
                "FROM process_monitoring_entries e " +
                "JOIN process_monitoring_reports r ON r.id = e.report_id " +
                "LEFT JOIN parameter_master p ON p.id = e.parameter_id" +
                where +
                " GROUP BY e.inspection_result, p.parameter_name ORDER BY cnt DESC";

        Query query = entityManager.createNativeQuery(entrySql);
        if (dateFrom != null) query.setParameter("dateFrom", dateFrom);
        if (dateTo != null) query.setParameter("dateTo", dateTo);
        if (lineId != null) query.setParameter("lineId", lineId);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        long passCount = 0, failCount = 0, total = 0;
        List<ChartDataPoint> failureFreq = new ArrayList<>();

        for (Object[] row : rows) {
            String result = (String) row[0];
            String paramName = (String) row[1];
            long cnt = ((Number) row[2]).longValue();
            total += cnt;
            if ("PASS".equals(result)) passCount += cnt;
            else if ("FAIL".equals(result)) {
                failCount += cnt;
                if (paramName != null) {
                    failureFreq.add(new ChartDataPoint(paramName, cnt));
                }
            }
        }

        double stability = total > 0 ? (double) passCount / total * 100 : 0;

        String outOfSpecSql = "SELECT p.parameter_name, COUNT(*) FROM process_monitoring_entries e " +
                "JOIN process_monitoring_reports r ON r.id = e.report_id " +
                "JOIN parameter_master p ON p.id = e.parameter_id " +
                "WHERE e.inspection_result = 'FAIL'" +
                (dateFrom != null ? " AND r.report_date >= :dateFrom" : "") +
                (dateTo != null ? " AND r.report_date <= :dateTo" : "") +
                (lineId != null ? " AND r.line_id = :lineId" : "") +
                " GROUP BY p.parameter_name ORDER BY COUNT(*) DESC";

        query = entityManager.createNativeQuery(outOfSpecSql);
        if (dateFrom != null) query.setParameter("dateFrom", dateFrom);
        if (dateTo != null) query.setParameter("dateTo", dateTo);
        if (lineId != null) query.setParameter("lineId", lineId);

        @SuppressWarnings("unchecked")
        List<Object[]> outOfSpecRows = query.getResultList();
        List<ChartDataPoint> outOfSpec = new ArrayList<>();
        for (Object[] row : outOfSpecRows) {
            outOfSpec.add(new ChartDataPoint((String) row[0], ((Number) row[1]).longValue()));
        }

        long totalReports = countReports("process_monitoring_reports", dateFrom, dateTo, null, lineId);

        List<KPICard> kpis = List.of(
                KPICard.builder().label("Process Stability").value(String.format("%.1f", stability)).unit("%").build(),
                KPICard.builder().label("Total Reports").value(String.valueOf(totalReports)).unit("").build(),
                KPICard.builder().label("Total Entries").value(String.valueOf(total)).unit("").build(),
                KPICard.builder().label("Failures").value(String.valueOf(failCount)).unit("").build()
        );

        return ProcessMonitoringKPIResponse.builder()
                .kpiCards(kpis)
                .outOfSpecParameters(outOfSpec)
                .failureFrequency(failureFreq)
                .build();
    }

    public ProductivityKPIResponse getProductivityKPIs(LocalDate dateFrom, LocalDate dateTo,
                                                       Long shiftId, Long lineId) {
        String base = REPORT_UNION;
        String where = buildWhereClause(dateFrom, dateTo, shiftId, lineId);
        String from = "FROM (" + base + ") r" + where;

        long total = count(from, dateFrom, dateTo, shiftId, lineId);

        String perDaySql = "SELECT r.report_date, COUNT(*) FROM (" + base + ") r" +
                buildWhereClause(dateFrom, dateTo, shiftId, lineId) +
                " GROUP BY r.report_date ORDER BY r.report_date";
        List<TrendPoint> perDay = executeTrendQuery(perDaySql, dateFrom, dateTo, shiftId, lineId);

        String perShiftSql = "SELECT s.name, COUNT(*) FROM (" + base + ") r " +
                "JOIN shifts s ON s.id = r.shift_id" +
                buildWhereClause(dateFrom, dateTo, null, lineId) +
                " GROUP BY s.name ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> perShift = executeChartQuery(perShiftSql, dateFrom, dateTo, null, lineId);

        String perOperatorSql = "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), " +
                "COUNT(*) FROM (" + base + ") r " +
                "JOIN users u ON u.id = r.created_by" +
                buildWhereClause(dateFrom, dateTo, shiftId, lineId) +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> perOperator = executeChartQuery(perOperatorSql, dateFrom, dateTo, shiftId, lineId);

        double avgApprovalHours = computeAvgApprovalHours(base, dateFrom, dateTo, shiftId, lineId);

        List<KPICard> kpis = List.of(
                KPICard.builder().label("Total Reports").value(String.valueOf(total)).unit("").build(),
                KPICard.builder().label("Avg Approval Time").value(String.format("%.1f", avgApprovalHours)).unit("hrs").build()
        );

        return ProductivityKPIResponse.builder()
                .kpiCards(kpis)
                .reportsPerDay(perDay)
                .reportsPerShift(perShift)
                .reportsPerOperator(perOperator)
                .build();
    }

    public TimeAnalyticsResponse getTimeTrends(LocalDate dateFrom, LocalDate dateTo,
                                               Long shiftId, Long lineId) {
        String base = REPORT_UNION;
        String where = buildWhereClause(dateFrom, dateTo, shiftId, lineId);
        String from = "FROM (" + base + ") r" + where;

        String dailySql = "SELECT r.report_date, COUNT(*) " + from +
                " GROUP BY r.report_date ORDER BY r.report_date";
        String weeklySql = "SELECT DATE_TRUNC('week', r.report_date)::date, COUNT(*) " + from +
                " GROUP BY DATE_TRUNC('week', r.report_date) ORDER BY 1";
        String monthlySql = "SELECT DATE_TRUNC('month', r.report_date)::date, COUNT(*) " + from +
                " GROUP BY DATE_TRUNC('month', r.report_date) ORDER BY 1";
        String yearlySql = "SELECT DATE_TRUNC('year', r.report_date)::date, COUNT(*) " + from +
                " GROUP BY DATE_TRUNC('year', r.report_date) ORDER BY 1";

        return TimeAnalyticsResponse.builder()
                .dailyTrend(executeTrendQuery(dailySql, dateFrom, dateTo, shiftId, lineId))
                .weeklyTrend(executeTrendQuery(weeklySql, dateFrom, dateTo, shiftId, lineId))
                .monthlyTrend(executeTrendQuery(monthlySql, dateFrom, dateTo, shiftId, lineId))
                .yearlyTrend(executeTrendQuery(yearlySql, dateFrom, dateTo, shiftId, lineId))
                .build();
    }

    public LinePerformanceResponse getLinePerformance(LocalDate dateFrom, LocalDate dateTo) {
        String base = REPORT_UNION;
        String where = buildWhereClause(dateFrom, dateTo, null, null);

        String byLineSql = "SELECT l.name, COUNT(*) FROM (" + base + ") r " +
                "JOIN line_master l ON l.id = r.line_id" + where +
                " GROUP BY l.name ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> byLine = executeChartQuery(byLineSql, dateFrom, dateTo, null, null);

        String rejectionSql = "SELECT l.name, COUNT(*) FROM (" + base + ") r " +
                "JOIN line_master l ON l.id = r.line_id" + where +
                " AND r.status = 'REJECTED' GROUP BY l.name ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> rejections = executeChartQuery(rejectionSql, dateFrom, dateTo, null, null);

        String approvalSql = "SELECT l.name, " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'APPROVED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM (" + base + ") r JOIN line_master l ON l.id = r.line_id" + where +
                " GROUP BY l.name ORDER BY 2 DESC";
        List<ChartDataPoint> approvalRate = executeDoubleChartQuery(approvalSql, dateFrom, dateTo);

        return LinePerformanceResponse.builder()
                .reportsByLine(byLine)
                .rejectionsByLine(rejections)
                .approvalRateByLine(approvalRate)
                .build();
    }

    public ShiftPerformanceResponse getShiftPerformance(LocalDate dateFrom, LocalDate dateTo) {
        String base = REPORT_UNION;
        String where = buildWhereClause(dateFrom, dateTo, null, null);

        String byShiftSql = "SELECT s.name, COUNT(*) FROM (" + base + ") r " +
                "JOIN shifts s ON s.id = r.shift_id" + where +
                " GROUP BY s.name ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> byShift = executeChartQuery(byShiftSql, dateFrom, dateTo, null, null);

        String passRateSql = "SELECT s.name, " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'APPROVED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM (" + base + ") r JOIN shifts s ON s.id = r.shift_id" + where +
                " GROUP BY s.name ORDER BY 2 DESC";
        List<ChartDataPoint> passRate = executeDoubleChartQuery(passRateSql, dateFrom, dateTo);

        String failRateSql = "SELECT s.name, " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'REJECTED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM (" + base + ") r JOIN shifts s ON s.id = r.shift_id" + where +
                " GROUP BY s.name ORDER BY 2 DESC";
        List<ChartDataPoint> failRate = executeDoubleChartQuery(failRateSql, dateFrom, dateTo);

        return ShiftPerformanceResponse.builder()
                .reportsByShift(byShift)
                .passRateByShift(passRate)
                .failureRateByShift(failRate)
                .build();
    }

    public OperatorPerformanceResponse getOperatorPerformance(LocalDate dateFrom, LocalDate dateTo) {
        String base = REPORT_UNION;
        String where = buildWhereClause(dateFrom, dateTo, null, null);

        String submittedSql = "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), " +
                "COUNT(*) FROM (" + base + ") r " +
                "JOIN users u ON u.id = r.created_by" + where +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> submitted = executeChartQuery(submittedSql, dateFrom, dateTo, null, null);

        String approvalSql = "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'APPROVED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM (" + base + ") r JOIN users u ON u.id = r.created_by" + where +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY 2 DESC";
        List<ChartDataPoint> approvalPct = executeDoubleChartQuery(approvalSql, dateFrom, dateTo);

        String rejectionSql = "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'REJECTED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM (" + base + ") r JOIN users u ON u.id = r.created_by" + where +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY 2 DESC";
        List<ChartDataPoint> rejectionPct = executeDoubleChartQuery(rejectionSql, dateFrom, dateTo);

        return OperatorPerformanceResponse.builder()
                .reportsSubmitted(submitted)
                .approvalPercentage(approvalPct)
                .rejectionPercentage(rejectionPct)
                .build();
    }

    // ---- helper methods ----

    private String buildWhereClause(LocalDate dateFrom, LocalDate dateTo,
                                    Long shiftId, Long lineId) {
        StringBuilder sb = new StringBuilder(" WHERE 1=1");
        if (dateFrom != null) sb.append(" AND r.report_date >= :dateFrom");
        if (dateTo != null) sb.append(" AND r.report_date <= :dateTo");
        if (shiftId != null) sb.append(" AND r.shift_id = :shiftId");
        if (lineId != null) sb.append(" AND r.line_id = :lineId");
        return sb.toString();
    }

    private void setFilterParams(Query query, LocalDate dateFrom, LocalDate dateTo,
                                 Long shiftId, Long lineId) {
        if (dateFrom != null) query.setParameter("dateFrom", dateFrom);
        if (dateTo != null) query.setParameter("dateTo", dateTo);
        if (shiftId != null) query.setParameter("shiftId", shiftId);
        if (lineId != null) query.setParameter("lineId", lineId);
    }

    private void bindParamsIfPresent(Query query, String sql, LocalDate dateFrom, LocalDate dateTo,
                                     Long shiftId, Long lineId) {
        if (dateFrom != null && sql.contains(":dateFrom")) query.setParameter("dateFrom", dateFrom);
        if (dateTo != null && sql.contains(":dateTo")) query.setParameter("dateTo", dateTo);
        if (shiftId != null && sql.contains(":shiftId")) query.setParameter("shiftId", shiftId);
        if (lineId != null && sql.contains(":lineId")) query.setParameter("lineId", lineId);
    }

    private long count(String from) {
        Query query = entityManager.createNativeQuery("SELECT COUNT(*) " + from);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private long count(String from, LocalDate dateFrom, LocalDate dateTo,
                       Long shiftId, Long lineId) {
        String sql = "SELECT COUNT(*) " + from;
        Query query = entityManager.createNativeQuery(sql);
        bindParamsIfPresent(query, sql, dateFrom, dateTo, shiftId, lineId);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private long countWithCondition(String from, String condition) {
        String sql = "SELECT COUNT(*) " + from + " AND " + condition;
        Query query = entityManager.createNativeQuery(sql);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private long countWithCondition(String from, String condition, LocalDate dateFrom,
                                    LocalDate dateTo, Long shiftId, Long lineId) {
        String sql = "SELECT COUNT(*) " + from + " AND " + condition;
        Query query = entityManager.createNativeQuery(sql);
        bindParamsIfPresent(query, sql, dateFrom, dateTo, shiftId, lineId);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private long countReports(String table, LocalDate dateFrom, LocalDate dateTo,
                              Long shiftId, Long lineId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + table + " WHERE 1=1");
        if (dateFrom != null) sql.append(" AND report_date >= :dateFrom");
        if (dateTo != null) sql.append(" AND report_date <= :dateTo");
        if (shiftId != null) sql.append(" AND shift_id = :shiftId");
        if (lineId != null) sql.append(" AND line_id = :lineId");

        Query query = entityManager.createNativeQuery(sql.toString());
        setFilterParams(query, dateFrom, dateTo, shiftId, lineId);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private long countWithDateRange(String base, LocalDate dateFrom, LocalDate dateTo) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM (" + base + ") r WHERE 1=1");
        if (dateFrom != null) sql.append(" AND report_date >= :dateFrom");
        if (dateTo != null) sql.append(" AND report_date <= :dateTo");
        Query query = entityManager.createNativeQuery(sql.toString());
        if (dateFrom != null) query.setParameter("dateFrom", dateFrom);
        if (dateTo != null) query.setParameter("dateTo", dateTo);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    @SuppressWarnings("unchecked")
    private List<ChartDataPoint> aggregateByColumn(String from, String column, LocalDate dateFrom,
                                                   LocalDate dateTo, Long shiftId, Long lineId) {
        String sql = "SELECT " + column + ", COUNT(*) " + from + " GROUP BY " + column + " ORDER BY COUNT(*) DESC";
        Query query = entityManager.createNativeQuery(sql);
        bindParamsIfPresent(query, sql, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<ChartDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ChartDataPoint(
                    row[0] != null ? row[0].toString() : "UNKNOWN",
                    ((Number) row[1]).longValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ChartDataPoint> aggregateWithJoin(String from, String fkColumn,
                                                   String joinTable, String nameColumn,
                                                   LocalDate dateFrom, LocalDate dateTo,
                                                   Long shiftId, Long lineId) {
        // from contains "FROM (...) r WHERE 1=1 ...", extract the part before WHERE
        String whereClause = "";
        String baseFrom = from;
        int whereIdx = from.indexOf(" WHERE ");
        if (whereIdx >= 0) {
            whereClause = from.substring(whereIdx);
            baseFrom = from.substring(0, whereIdx);
        }
        String sql = "SELECT j." + nameColumn + ", COUNT(*) " + baseFrom +
                " JOIN " + joinTable + " j ON j.id = " + fkColumn +
                whereClause +
                " GROUP BY j." + nameColumn + " ORDER BY COUNT(*) DESC";
        Query query = entityManager.createNativeQuery(sql);
        bindParamsIfPresent(query, sql, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<ChartDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ChartDataPoint(
                    row[0] != null ? row[0].toString() : "UNKNOWN",
                    ((Number) row[1]).longValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ChartDataPoint> executeChartQuery(String sql, LocalDate dateFrom,
                                                   LocalDate dateTo, Long shiftId, Long lineId) {
        Query query = entityManager.createNativeQuery(sql);
        setFilterParams(query, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<ChartDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ChartDataPoint(
                    row[0] != null ? row[0].toString() : "",
                    ((Number) row[1]).longValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ChartDataPoint> executeDoubleChartQuery(String sql, LocalDate dateFrom,
                                                         LocalDate dateTo) {
        Query query = entityManager.createNativeQuery(sql);
        setFilterParams(query, dateFrom, dateTo, null, null);
        List<Object[]> rows = query.getResultList();
        List<ChartDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ChartDataPoint(
                    row[0] != null ? row[0].toString() : "",
                    Math.round(((Number) row[1]).doubleValue())));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<TrendPoint> executeTrendQuery(String sql, LocalDate dateFrom, LocalDate dateTo,
                                               Long shiftId, Long lineId) {
        Query query = entityManager.createNativeQuery(sql);
        bindParamsIfPresent(query, sql, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<TrendPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            Date d = (Date) row[0];
            result.add(TrendPoint.builder()
                    .date(d != null ? d.toLocalDate() : null)
                    .value(((Number) row[1]).longValue())
                    .build());
        }
        return result;
    }

    private double computeAvgApprovalHours(String base, LocalDate dateFrom, LocalDate dateTo,
                                           Long shiftId, Long lineId) {
        StringBuilder sql = new StringBuilder(
                "SELECT AVG(EXTRACT(EPOCH FROM (r.approved_at - r.created_at)) / 3600.0) " +
                "FROM (" + base + ") r WHERE r.status = 'APPROVED' AND r.approved_at IS NOT NULL");
        if (dateFrom != null) sql.append(" AND r.report_date >= :dateFrom");
        if (dateTo != null) sql.append(" AND r.report_date <= :dateTo");
        if (shiftId != null) sql.append(" AND r.shift_id = :shiftId");
        if (lineId != null) sql.append(" AND r.line_id = :lineId");

        Query query = entityManager.createNativeQuery(sql.toString());
        setFilterParams(query, dateFrom, dateTo, shiftId, lineId);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.doubleValue() : 0;
    }

    @SuppressWarnings("unchecked")
    private List<ChartDataPoint> getPassFailByType(LocalDate dateFrom, LocalDate dateTo,
                                                    Long shiftId, Long lineId) {
        String entryUnion = buildEntryUnion();
        String entryWhere = buildEntryWhere(dateFrom, dateTo, shiftId, lineId);

        String sql = "SELECT e.report_type, e.result, COUNT(*) FROM (" + entryUnion + ") e" +
                entryWhere +
                " GROUP BY e.report_type, e.result ORDER BY e.report_type, e.result";

        Query query = entityManager.createNativeQuery(sql);
        bindParamsIfPresent(query, sql, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<ChartDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ChartDataPoint(
                    row[0] + " - " + row[1],
                    ((Number) row[2]).longValue()));
        }
        return result;
    }

    private String buildEntryUnion() {
        return "SELECT 'PROCESS_MONITORING' as report_type, inspection_result as result, " +
               "report_id FROM process_monitoring_entries UNION ALL " +
               "SELECT 'CHEMICAL_CONSUMPTION', inspection_result, report_id " +
               "FROM chemical_consumption_entries UNION ALL " +
               "SELECT 'DAILY_STARTUP', inspection_result, report_id " +
               "FROM daily_startup_entries UNION ALL " +
               "SELECT 'FIRST_PIECE_INSPECTION', inspection_result, report_id " +
               "FROM first_piece_inspection_entries UNION ALL " +
               "SELECT 'DAILY_INSPECTION', inspection_result, report_id " +
               "FROM daily_inspection_entries UNION ALL " +
               "SELECT 'PDI', inspection_result, report_id " +
               "FROM pre_delivery_inspection_entries";
    }

    private String buildEntryWhere(LocalDate dateFrom, LocalDate dateTo,
                                   Long shiftId, Long lineId) {
        StringBuilder sb = new StringBuilder(" WHERE 1=1");
        sb.append(" AND e.result IN ('PASS','FAIL','NOT_APPLICABLE')");
        if (dateFrom != null) {
            sb.append(" AND EXISTS (SELECT 1 FROM process_monitoring_reports pm WHERE pm.id = e.report_id AND pm.report_date >= :dateFrom)");
        }
        return sb.toString();
    }

}
