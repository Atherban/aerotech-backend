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

/**
 * Analytics over the Generic Report Engine (Phase 4). Every metric is derived
 * from the module-driven tables — {@code report} (CompletedReport) with its
 * immutable snapshots, plus {@code recorded_value}/{@code recorded_process} for
 * entry-level figures. No report-specific legacy tables are consulted and no
 * report-type-specific Java code exists.
 *
 * <p>Entry-level PASS/FAIL is configuration-driven: a {@code recorded_value} is
 * PASS when its numeric {@code observed_value} lies within the frozen
 * {@code minimum_value}/{@code maximum_value} snapshot of the process parameter.
 * "Consumption" sums the numeric bounded observed values. All other aggregation
 * patterns mirror the legacy analytics service.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String REPORT_ALIAS = "r";

    /** Numeric values only — prevents casts of free-text observations. */
    private static final String NUMERIC =
            "v.observed_value ~ '^-?[0-9]+(\\.[0-9]+)?$'";

    /** A bounded, measurable value (has at least one spec boundary). */
    private static final String BOUNDED =
            "(v.minimum_value IS NOT NULL OR v.maximum_value IS NOT NULL)";

    private static final String WITHIN_SPEC =
            "(" + NUMERIC + " AND " + BOUNDED +
            " AND (v.minimum_value IS NULL OR CAST(v.observed_value AS NUMERIC) >= v.minimum_value)" +
            " AND (v.maximum_value IS NULL OR CAST(v.observed_value AS NUMERIC) <= v.maximum_value))";

    private static final String OUT_OF_SPEC =
            "(" + NUMERIC + " AND " + BOUNDED + " AND NOT " + WITHIN_SPEC + ")";

    private static final String VALUE_FROM =
            "FROM recorded_value v " +
            "JOIN recorded_process rp ON rp.id = v.recorded_process_id " +
            "JOIN report_session s ON s.id = rp.session_id " +
            "JOIN report r ON r.session_id = s.id ";

    public ReportOverviewResponse getReportOverview(LocalDate dateFrom, LocalDate dateTo,
                                                    Long shiftId, Long lineId) {
        String where = buildWhere(dateFrom, dateTo, shiftId, lineId);
        String params = where;

        long total = countReports(where, dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byType = executeChartQuery(
                "SELECT r.module_name, COUNT(*) FROM report r" + where +
                " GROUP BY r.module_name ORDER BY COUNT(*) DESC", params, dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byStatus = executeChartQuery(
                "SELECT r.status, COUNT(*) FROM report r" + where +
                " GROUP BY r.status ORDER BY COUNT(*) DESC", params, dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byShift = executeChartQuery(
                "SELECT r.shift_name, COUNT(*) FROM report r" + where +
                " AND r.shift_id IS NOT NULL GROUP BY r.shift_name ORDER BY COUNT(*) DESC",
                params, dateFrom, dateTo, shiftId, lineId);
        List<ChartDataPoint> byLine = executeChartQuery(
                "SELECT r.line_name, COUNT(*) FROM report r" + where +
                " AND r.line_id IS NOT NULL GROUP BY r.line_name ORDER BY COUNT(*) DESC",
                params, dateFrom, dateTo, shiftId, lineId);

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
        String where = buildWhere(dateFrom, dateTo, shiftId, lineId);

        long total = countReports(where, dateFrom, dateTo, shiftId, lineId);
        long approved = countWithStatus("APPROVED", where, dateFrom, dateTo, shiftId, lineId);
        long rejected = countWithStatus("REJECTED", where, dateFrom, dateTo, shiftId, lineId);
        long submitted = countWithStatus("SUBMITTED", where, dateFrom, dateTo, shiftId, lineId);

        double approvalRate = total > 0 ? (double) approved / total * 100 : 0;
        double rejectionRate = total > 0 ? (double) rejected / total * 100 : 0;

        ValueStats stats = valueStats(where, dateFrom, dateTo, shiftId, lineId);
        double passRate = stats.total > 0 ? (double) stats.pass / stats.total * 100 : 0;
        double failRate = stats.total > 0 ? (double) stats.fail / stats.total * 100 : 0;

        List<TrendPoint> dailyTrend = executeTrendQuery(
                "SELECT r.started_at::date, COUNT(*) FROM report r" + where +
                " GROUP BY r.started_at::date ORDER BY r.started_at::date",
                where, dateFrom, dateTo, shiftId, lineId);

        List<ChartDataPoint> passFailByType = passFailByModule(where, dateFrom, dateTo, shiftId, lineId);

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
        String where = buildWhere(dateFrom, dateTo, null, lineId);

        long total = countReports(where, dateFrom, dateTo, null, lineId);

        Query todayQuery = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM report WHERE created_at::date = CURRENT_DATE");
        long dailyCount = ((Number) todayQuery.getSingleResult()).longValue();

        String sumWhere = where + " AND " + NUMERIC + " AND " + BOUNDED;

        List<TrendPoint> dailyTrend = executeSumTrendQuery(
                "SELECT r.started_at::date, COALESCE(SUM(CAST(v.observed_value AS NUMERIC)), 0) " +
                VALUE_FROM + sumWhere + " GROUP BY r.started_at::date ORDER BY r.started_at::date",
                where, dateFrom, dateTo, null, lineId);

        List<TrendPoint> weeklyTrend = executeSumTrendQuery(
                "SELECT DATE_TRUNC('week', r.started_at)::date, COALESCE(SUM(CAST(v.observed_value AS NUMERIC)), 0) " +
                VALUE_FROM + sumWhere + " GROUP BY DATE_TRUNC('week', r.started_at) ORDER BY 1",
                where, dateFrom, dateTo, null, lineId);

        List<TrendPoint> monthlyTrend = executeSumTrendQuery(
                "SELECT DATE_TRUNC('month', r.started_at)::date, COALESCE(SUM(CAST(v.observed_value AS NUMERIC)), 0) " +
                VALUE_FROM + sumWhere + " GROUP BY DATE_TRUNC('month', r.started_at) ORDER BY 1",
                where, dateFrom, dateTo, null, lineId);

        List<ChartDataPoint> byLine = executeDoubleChartQuery(
                "SELECT r.line_name, COALESCE(SUM(CAST(v.observed_value AS NUMERIC)), 0) " +
                VALUE_FROM + sumWhere + " AND r.line_id IS NOT NULL" +
                " GROUP BY r.line_name ORDER BY 2 DESC", where, dateFrom, dateTo, null, lineId);

        List<KPICard> kpis = List.of(
                KPICard.builder().label("Total Reports").value(String.valueOf(total)).unit("").build(),
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
        String where = buildWhere(dateFrom, dateTo, null, lineId);

        ValueStats stats = valueStats(where, dateFrom, dateTo, null, lineId);
        double stability = stats.total > 0 ? (double) stats.pass / stats.total * 100 : 0;

        long totalReports = countReports(where, dateFrom, dateTo, null, lineId);

        String failSql = "SELECT v.parameter_name, COUNT(*) " + VALUE_FROM +
                where + " AND " + OUT_OF_SPEC +
                " GROUP BY v.parameter_name ORDER BY COUNT(*) DESC";
        List<ChartDataPoint> outOfSpec = executeChartQuery(failSql, where, dateFrom, dateTo, null, lineId);

        List<KPICard> kpis = List.of(
                KPICard.builder().label("Process Stability").value(String.format("%.1f", stability)).unit("%").build(),
                KPICard.builder().label("Total Reports").value(String.valueOf(totalReports)).unit("").build(),
                KPICard.builder().label("Total Entries").value(String.valueOf(stats.total)).unit("").build(),
                KPICard.builder().label("Failures").value(String.valueOf(stats.fail)).unit("").build()
        );

        return ProcessMonitoringKPIResponse.builder()
                .kpiCards(kpis)
                .outOfSpecParameters(outOfSpec)
                .failureFrequency(new ArrayList<>(outOfSpec))
                .build();
    }

    public ProductivityKPIResponse getProductivityKPIs(LocalDate dateFrom, LocalDate dateTo,
                                                       Long shiftId, Long lineId) {
        String where = buildWhere(dateFrom, dateTo, shiftId, lineId);

        long total = countReports(where, dateFrom, dateTo, shiftId, lineId);

        List<TrendPoint> perDay = executeTrendQuery(
                "SELECT r.started_at::date, COUNT(*) FROM report r" + where +
                " GROUP BY r.started_at::date ORDER BY r.started_at::date",
                where, dateFrom, dateTo, shiftId, lineId);

        List<ChartDataPoint> perShift = executeChartQuery(
                "SELECT r.shift_name, COUNT(*) FROM report r" + where +
                " AND r.shift_id IS NOT NULL GROUP BY r.shift_name ORDER BY COUNT(*) DESC",
                where, dateFrom, dateTo, shiftId, lineId);

        List<ChartDataPoint> perOperator = executeChartQuery(
                "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), COUNT(*) " +
                "FROM report r JOIN users u ON u.id = r.created_by" + where +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY COUNT(*) DESC",
                where, dateFrom, dateTo, shiftId, lineId);

        double avgApprovalHours = computeAvgApprovalHours(where, dateFrom, dateTo, shiftId, lineId);

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
        String where = buildWhere(dateFrom, dateTo, shiftId, lineId);

        List<TrendPoint> daily = executeTrendQuery(
                "SELECT r.started_at::date, COUNT(*) FROM report r" + where +
                " GROUP BY r.started_at::date ORDER BY r.started_at::date",
                where, dateFrom, dateTo, shiftId, lineId);
        List<TrendPoint> weekly = executeTrendQuery(
                "SELECT DATE_TRUNC('week', r.started_at)::date, COUNT(*) FROM report r" + where +
                " GROUP BY DATE_TRUNC('week', r.started_at) ORDER BY 1",
                where, dateFrom, dateTo, shiftId, lineId);
        List<TrendPoint> monthly = executeTrendQuery(
                "SELECT DATE_TRUNC('month', r.started_at)::date, COUNT(*) FROM report r" + where +
                " GROUP BY DATE_TRUNC('month', r.started_at) ORDER BY 1",
                where, dateFrom, dateTo, shiftId, lineId);
        List<TrendPoint> yearly = executeTrendQuery(
                "SELECT DATE_TRUNC('year', r.started_at)::date, COUNT(*) FROM report r" + where +
                " GROUP BY DATE_TRUNC('year', r.started_at) ORDER BY 1",
                where, dateFrom, dateTo, shiftId, lineId);

        return TimeAnalyticsResponse.builder()
                .dailyTrend(daily)
                .weeklyTrend(weekly)
                .monthlyTrend(monthly)
                .yearlyTrend(yearly)
                .build();
    }

    public LinePerformanceResponse getLinePerformance(LocalDate dateFrom, LocalDate dateTo) {
        String where = buildWhere(dateFrom, dateTo, null, null);

        List<ChartDataPoint> byLine = executeChartQuery(
                "SELECT r.line_name, COUNT(*) FROM report r" + where +
                " AND r.line_id IS NOT NULL GROUP BY r.line_name ORDER BY COUNT(*) DESC",
                where, dateFrom, dateTo, null, null);
        List<ChartDataPoint> rejections = executeChartQuery(
                "SELECT r.line_name, COUNT(*) FROM report r" + where +
                " AND r.line_id IS NOT NULL AND r.status = 'REJECTED'" +
                " GROUP BY r.line_name ORDER BY COUNT(*) DESC",
                where, dateFrom, dateTo, null, null);
        List<ChartDataPoint> approvalRate = executeDoubleChartQuery(
                "SELECT r.line_name, " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'APPROVED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM report r" + where + " AND r.line_id IS NOT NULL GROUP BY r.line_name ORDER BY 2 DESC",
                where, dateFrom, dateTo, null, null);

        return LinePerformanceResponse.builder()
                .reportsByLine(byLine)
                .rejectionsByLine(rejections)
                .approvalRateByLine(approvalRate)
                .build();
    }

    public ShiftPerformanceResponse getShiftPerformance(LocalDate dateFrom, LocalDate dateTo) {
        String where = buildWhere(dateFrom, dateTo, null, null);

        List<ChartDataPoint> byShift = executeChartQuery(
                "SELECT r.shift_name, COUNT(*) FROM report r" + where +
                " AND r.shift_id IS NOT NULL GROUP BY r.shift_name ORDER BY COUNT(*) DESC",
                where, dateFrom, dateTo, null, null);
        List<ChartDataPoint> passRate = executeDoubleChartQuery(
                "SELECT r.shift_name, " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'APPROVED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM report r" + where + " AND r.shift_id IS NOT NULL GROUP BY r.shift_name ORDER BY 2 DESC",
                where, dateFrom, dateTo, null, null);
        List<ChartDataPoint> failRate = executeDoubleChartQuery(
                "SELECT r.shift_name, " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'REJECTED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM report r" + where + " AND r.shift_id IS NOT NULL GROUP BY r.shift_name ORDER BY 2 DESC",
                where, dateFrom, dateTo, null, null);

        return ShiftPerformanceResponse.builder()
                .reportsByShift(byShift)
                .passRateByShift(passRate)
                .failureRateByShift(failRate)
                .build();
    }

    public OperatorPerformanceResponse getOperatorPerformance(LocalDate dateFrom, LocalDate dateTo) {
        String where = buildWhere(dateFrom, dateTo, null, null);

        List<ChartDataPoint> submitted = executeChartQuery(
                "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), COUNT(*) " +
                "FROM report r JOIN users u ON u.id = r.created_by" + where +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY COUNT(*) DESC",
                where, dateFrom, dateTo, null, null);
        List<ChartDataPoint> approvalPct = executeDoubleChartQuery(
                "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'APPROVED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM report r JOIN users u ON u.id = r.created_by" + where +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY 2 DESC",
                where, dateFrom, dateTo, null, null);
        List<ChartDataPoint> rejectionPct = executeDoubleChartQuery(
                "SELECT COALESCE(CONCAT(u.first_name, ' ', u.last_name), u.employee_id), " +
                "ROUND(COUNT(*) FILTER (WHERE r.status = 'REJECTED') * 100.0 / NULLIF(COUNT(*), 0), 1) " +
                "FROM report r JOIN users u ON u.id = r.created_by" + where +
                " GROUP BY u.first_name, u.last_name, u.employee_id ORDER BY 2 DESC",
                where, dateFrom, dateTo, null, null);

        return OperatorPerformanceResponse.builder()
                .reportsSubmitted(submitted)
                .approvalPercentage(approvalPct)
                .rejectionPercentage(rejectionPct)
                .build();
    }

    // ---- helpers ----

    private String buildWhere(LocalDate dateFrom, LocalDate dateTo, Long shiftId, Long lineId) {
        StringBuilder sb = new StringBuilder(" WHERE 1=1");
        if (dateFrom != null) sb.append(" AND r.started_at::date >= :dateFrom");
        if (dateTo != null) sb.append(" AND r.started_at::date <= :dateTo");
        if (shiftId != null) sb.append(" AND r.shift_id = :shiftId");
        if (lineId != null) sb.append(" AND r.line_id = :lineId");
        return sb.toString();
    }

    private void bindWhereParams(Query query, String sql, LocalDate dateFrom, LocalDate dateTo,
                                 Long shiftId, Long lineId) {
        if (dateFrom != null && sql.contains(":dateFrom")) query.setParameter("dateFrom", dateFrom);
        if (dateTo != null && sql.contains(":dateTo")) query.setParameter("dateTo", dateTo);
        if (shiftId != null && sql.contains(":shiftId")) query.setParameter("shiftId", shiftId);
        if (lineId != null && sql.contains(":lineId")) query.setParameter("lineId", lineId);
    }

    private long countReports(String where, LocalDate dateFrom, LocalDate dateTo,
                              Long shiftId, Long lineId) {
        String sql = "SELECT COUNT(*) FROM report r" + where;
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private long countWithStatus(String status, String where, LocalDate dateFrom, LocalDate dateTo,
                                 Long shiftId, Long lineId) {
        String sql = "SELECT COUNT(*) FROM report r" + where + " AND r.status = '" + status + "'";
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0;
    }

    private ValueStats valueStats(String where, LocalDate dateFrom, LocalDate dateTo,
                                  Long shiftId, Long lineId) {
        String sql = "SELECT " +
                "COUNT(*) AS total, " +
                "COUNT(*) FILTER (WHERE " + WITHIN_SPEC + ") AS pass, " +
                "COUNT(*) FILTER (WHERE " + OUT_OF_SPEC + ") AS fail " +
                VALUE_FROM + where;
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
        Object[] row = (Object[]) query.getSingleResult();
        return new ValueStats(
                ((Number) row[0]).longValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue());
    }

    @SuppressWarnings("unchecked")
    private List<ChartDataPoint> passFailByModule(String where, LocalDate dateFrom, LocalDate dateTo,
                                                  Long shiftId, Long lineId) {
        String sql = "SELECT r.module_name, " +
                "COUNT(*) FILTER (WHERE " + WITHIN_SPEC + ") AS pass, " +
                "COUNT(*) FILTER (WHERE " + OUT_OF_SPEC + ") AS fail " +
                VALUE_FROM + where + " GROUP BY r.module_name ORDER BY r.module_name";
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<ChartDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            String module = (String) row[0];
            long pass = ((Number) row[1]).longValue();
            long fail = ((Number) row[2]).longValue();
            if (pass > 0) result.add(new ChartDataPoint(module + " - PASS", pass));
            if (fail > 0) result.add(new ChartDataPoint(module + " - FAIL", fail));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<TrendPoint> executeTrendQuery(String sql, String where, LocalDate dateFrom,
                                               LocalDate dateTo, Long shiftId, Long lineId) {
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
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

    @SuppressWarnings("unchecked")
    private List<TrendPoint> executeSumTrendQuery(String sql, String where, LocalDate dateFrom,
                                                  LocalDate dateTo, Long shiftId, Long lineId) {
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<TrendPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            Date d = (Date) row[0];
            result.add(TrendPoint.builder()
                    .date(d != null ? d.toLocalDate() : null)
                    .value(Math.round(((Number) row[1]).doubleValue()))
                    .build());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ChartDataPoint> executeChartQuery(String sql, String where, LocalDate dateFrom,
                                                   LocalDate dateTo, Long shiftId, Long lineId) {
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
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
    private List<ChartDataPoint> executeDoubleChartQuery(String sql, String where, LocalDate dateFrom,
                                                         LocalDate dateTo, Long shiftId, Long lineId) {
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
        List<Object[]> rows = query.getResultList();
        List<ChartDataPoint> result = new ArrayList<>();
        for (Object[] row : rows) {
            result.add(new ChartDataPoint(
                    row[0] != null ? row[0].toString() : "",
                    Math.round(((Number) row[1]).doubleValue())));
        }
        return result;
    }

    private double computeAvgApprovalHours(String where, LocalDate dateFrom, LocalDate dateTo,
                                           Long shiftId, Long lineId) {
        String sql = "SELECT AVG(EXTRACT(EPOCH FROM (r.approved_at - r.created_at)) / 3600.0) " +
                "FROM report r WHERE r.status = 'APPROVED' AND r.approved_at IS NOT NULL" +
                where.replaceFirst(" WHERE 1=1", "");
        Query query = entityManager.createNativeQuery(sql);
        bindWhereParams(query, sql, dateFrom, dateTo, shiftId, lineId);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.doubleValue() : 0;
    }

    private record ValueStats(long total, long pass, long fail) {
    }

}
