package com.aerotech.ced_ops_backend.report.globalsearch.service;

import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.request.UnifiedSearchRequest;
import com.aerotech.ced_ops_backend.report.globalsearch.dto.response.UnifiedSearchResultItem;
import com.aerotech.ced_ops_backend.report.globalsearch.util.UnifiedSearchQueryBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class UnifiedSearchService {

    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<UnifiedSearchResultItem> search(UnifiedSearchRequest request) {
        Map<String, Object> params = UnifiedSearchQueryBuilder.extractParams(request);

        long totalElements = executeCountQuery(request, params);

        int page = request.pageOrDefault();
        int size = request.sizeOrDefault();
        int offset = page * size;

        List<UnifiedSearchResultItem> content = executeDataQuery(request, params, offset, size);

        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;

        log.info("Unified search: type={}, total={}, page={}, size={}",
                request.getType(), totalElements, page, size);

        return PageResponse.<UnifiedSearchResultItem>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1 || totalPages == 0)
                .build();
    }

    private long executeCountQuery(UnifiedSearchRequest request, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(UnifiedSearchQueryBuilder.buildCountQuery(request));
        params.forEach(query::setParameter);
        Number result = (Number) query.getSingleResult();
        return result != null ? result.longValue() : 0L;
    }

    @SuppressWarnings("unchecked")
    private List<UnifiedSearchResultItem> executeDataQuery(
            UnifiedSearchRequest request, Map<String, Object> params, int offset, int size
    ) {
        Query query = entityManager.createNativeQuery(UnifiedSearchQueryBuilder.buildDataQuery(request));
        params.forEach(query::setParameter);
        query.setParameter("offset", offset);
        query.setParameter("size", size);

        List<Object[]> rows = query.getResultList();
        List<UnifiedSearchResultItem> items = new ArrayList<>();

        for (Object[] row : rows) {
            items.add(mapRow(row));
        }
        return items;
    }

    private UnifiedSearchResultItem mapRow(Object[] row) {
        String type = (String) row[0];
        Long id = row[1] != null ? ((Number) row[1]).longValue() : null;
        String title = (String) row[2];
        String subtitle = (String) row[3];
        String reportType = (String) row[4];
        String status = (String) row[5];
        String shiftName = (String) row[6];
        String lineName = (String) row[7];
        String actor = (String) row[8];
        Date reportDate = (Date) row[9];
        Timestamp createdAt = (Timestamp) row[10];

        return UnifiedSearchResultItem.builder()
                .type(type)
                .id(id)
                .title(title)
                .subtitle(subtitle)
                .reportType(reportType)
                .status(status)
                .shiftName(shiftName)
                .lineName(lineName)
                .actor(actor)
                .reportDate(reportDate != null ? reportDate.toLocalDate() : null)
                .createdAt(createdAt != null ? createdAt.toLocalDateTime() : null)
                .build();
    }

}
