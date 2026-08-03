package com.aerotech.ced_ops_backend.report.globalsearch.service;

import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.request.GlobalSearchRequest;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.response.GlobalSearchResultItem;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.response.SearchSuggestionsResponse;
import com.aerotech.ced_ops_backend.report.globalsearch.util.GlobalSearchQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GlobalSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<GlobalSearchResultItem> search(GlobalSearchRequest request) {
        Map<String, Object> params = GlobalSearchQueryBuilder.extractParams(request);

        long totalElements = executeCountQuery(request, params);

        int page = request.getPage();
        int size = request.getSize();
        int offset = page * size;

        List<GlobalSearchResultItem> content = executeDataQuery(request, params, offset, size);

        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size): 0;

        log.info("Global search: totalResults={}, page={}, size={}", totalElements, page, size);

        return PageResponse.<GlobalSearchResultItem>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    public SearchSuggestionsResponse getSuggestions(String query) {
        String pattern = "%" + query + "%";

        List<String> reportNumbers = fetchReportNumberSuggestions(pattern);
        List<String> employeeNames = fetchEmployeeNameSuggestions(pattern);
        List<String> lines = fetchLineSuggestions(pattern);
        List<String> parameters = fetchParameterSuggestions(pattern);

        return SearchSuggestionsResponse.builder()
                .reportNumbers(reportNumbers)
                .employeeNames(employeeNames)
                .lines(lines)
                .parameters(parameters)
                .build();
    }

    private long executeCountQuery(GlobalSearchRequest request, Map<String, Object> params) {
        String sql = GlobalSearchQueryBuilder.buildCountQuery(request);
        Query query = entityManager.createNativeQuery(sql);
        params.forEach(query::setParameter);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue(): 0L;
    }

    @SuppressWarnings("unchecked")
    private List<GlobalSearchResultItem> executeDataQuery(
            GlobalSearchRequest request, Map<String, Object> params, int offset, int size
    ) {
        String sql = GlobalSearchQueryBuilder.buildDataQuery(request);
        Query query = entityManager.createNativeQuery(sql);
        params.forEach(query::setParameter);
        query.setFirstResult(offset);
        query.setMaxResults(size);

        List<Object[]> rows = query.getResultList();
        List<GlobalSearchResultItem> items = new ArrayList<>();

        for (Object[] row: rows) {
            Long id = row[0] != null ? ((Number) row[0]).longValue(): null;
            String reportNumber = (String) row[1];
            String reportType = (String) row[2];
            Date reportDate = (Date) row[3];
            String status = (String) row[4];
            String shiftName = (String) row[5];
            String lineName = (String) row[6];
            String createdBy = (String) row[7];
            String approvedBy = (String) row[8];
            String summary = (String) row[9];

            items.add(GlobalSearchResultItem.builder()
                    .id(id)
                    .reportNumber(reportNumber)
                    .reportType(reportType)
                    .reportDate(reportDate != null ? reportDate.toLocalDate(): null)
                    .status(status)
                    .shiftName(shiftName)
                    .lineName(lineName)
                    .createdBy(createdBy)
                    .approvedBy(approvedBy)
                    .summary(summary)
                    .build());
        }

        return items;
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchReportNumberSuggestions(String pattern) {
        String sql = """
                SELECT report_number FROM process_monitoring_reports WHERE report_number ILIKE :pattern
                UNION ALL SELECT report_number FROM chemical_consumption_reports WHERE report_number ILIKE :pattern
                UNION ALL SELECT report_number FROM daily_startup_reports WHERE report_number ILIKE :pattern
                UNION ALL SELECT report_number FROM first_piece_inspection_reports WHERE report_number ILIKE :pattern
                UNION ALL SELECT report_number FROM daily_inspection_reports WHERE report_number ILIKE :pattern
                UNION ALL SELECT report_number FROM pre_delivery_inspection_reports WHERE report_number ILIKE :pattern
                ORDER BY report_number LIMIT 10
                """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("pattern", pattern);
        return (List<String>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchEmployeeNameSuggestions(String pattern) {
        String sql = """
                SELECT DISTINCT CONCAT(first_name, ' ', last_name)
                FROM users
                WHERE CONCAT(first_name, ' ', last_name) ILIKE :pattern
                OR employee_id ILIKE :pattern
                ORDER BY 1 LIMIT 10
                """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("pattern", pattern);
        return (List<String>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchLineSuggestions(String pattern) {
        String sql = """
                SELECT name FROM line_master WHERE name ILIKE :pattern ORDER BY name LIMIT 10
                """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("pattern", pattern);
        return (List<String>) query.getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<String> fetchParameterSuggestions(String pattern) {
        String sql = """
                SELECT DISTINCT parameter_name FROM parameter_master
                WHERE parameter_name ILIKE :pattern
                ORDER BY parameter_name LIMIT 10
                """;
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("pattern", pattern);
        return (List<String>) query.getResultList();
    }

}
