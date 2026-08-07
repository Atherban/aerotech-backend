package com.aerotech.ced_ops_backend.report.globalsearch.util;

import com.aerotech.ced_ops_backend.report.globalsearch.dto.request.UnifiedSearchRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnifiedSearchQueryBuilderTest {

    @Test
    void reportBranchReadsFromEngineReportTableOnly() {
        UnifiedSearchRequest request = new UnifiedSearchRequest();
        request.setType(UnifiedSearchRequest.TYPE_REPORT);

        String sql = UnifiedSearchQueryBuilder.buildDataQuery(request);

        assertThat(sql).contains("FROM report r");
        assertThat(sql).contains("r.module_name AS report_type");
        assertThat(sql).contains("r.started_at::date AS report_date");
        assertThat(sql).contains("r.shift_name AS shift_name");
        assertThat(sql).contains("r.line_name AS line_name");
        assertThat(sql).contains("r.module_id, r.module_type_id");
        assertThat(sql).doesNotContain("process_monitoring_reports");
        assertThat(sql).doesNotContain("daily_inspection_reports");
    }

    @Test
    void defaultSearchIncludesAllBranchesWithEngineReportOnly() {
        String sql = UnifiedSearchQueryBuilder.buildDataQuery(null);

        assertThat(sql).contains("FROM report r");
        assertThat(sql).contains("FROM users u");
        assertThat(sql).contains("FROM parameter p");
        assertThat(sql).doesNotContain("_reports");
        assertThat(sql).doesNotContain("parameter_master");
    }

    @Test
    void moduleAndModuleTypeFiltersAreApplied() {
        UnifiedSearchRequest request = new UnifiedSearchRequest();
        request.setModuleId(3L);
        request.setModuleTypeId(9L);
        request.setShiftId(2L);
        request.setLineId(4L);

        String sql = UnifiedSearchQueryBuilder.buildDataQuery(request);

        assertThat(sql).contains("module_id = :moduleId");
        assertThat(sql).contains("module_type_id = :moduleTypeId");
        assertThat(sql).contains("shift_id = :shiftId");
        assertThat(sql).contains("line_id = :lineId");
    }

    @Test
    void extractParamsIncludesModuleFilters() {
        UnifiedSearchRequest request = new UnifiedSearchRequest();
        request.setModuleId(3L);
        request.setModuleTypeId(9L);

        Map<String, Object> params = UnifiedSearchQueryBuilder.extractParams(request);

        assertThat(params).containsEntry("moduleId", 3L);
        assertThat(params).containsEntry("moduleTypeId", 9L);
        assertThat(params).doesNotContainKey("shiftId");
    }

    @Test
    void reportNumberAndStatusFiltersRemain() {
        UnifiedSearchRequest request = new UnifiedSearchRequest();
        request.setReportNumber("PMR-1");
        request.setStatus("SUBMITTED");
        request.setDateFrom(java.time.LocalDate.of(2026, 1, 1));

        String sql = UnifiedSearchQueryBuilder.buildDataQuery(request);

        assertThat(sql).contains("title ILIKE '%' || :reportNumber || '%'");
        assertThat(sql).contains("status = :status");
        assertThat(sql).contains("report_date >= :dateFrom");
    }
}
