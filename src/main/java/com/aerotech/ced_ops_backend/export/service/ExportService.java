package com.aerotech.ced_ops_backend.export.service;

import com.aerotech.ced_ops_backend.common.enums.ExportFormat;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.response.ApiResponse;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.export.dto.request.ExportRequest;
import com.aerotech.ced_ops_backend.export.dto.response.ExportJobResponse;
import com.aerotech.ced_ops_backend.export.entity.ExportJob;
import com.aerotech.ced_ops_backend.export.repository.ExportJobRepository;
import com.aerotech.ced_ops_backend.export.service.ExportStrategy;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExportJobRepository exportJobRepository;
    private final UserRepository userRepository;
    private final List<ExportStrategy> strategies;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${app.storage.export-dir:exports}")
    private String exportDir;

    private Path exportPath;

    @PostConstruct
    public void init() {
        exportPath = Paths.get(exportDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(exportPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create export directory", e);
        }
    }

    @Transactional
    public ExportJobResponse export(ExportRequest request) {
        Long userId = currentUserId();
        ExportFormat format = parseFormat(request.getFormat());

        ExportJob job = ExportJob.builder()
                .source(request.getSource())
                .format(format)
                .filters(buildFiltersJson(request))
                .status("PROCESSING")
                .createdBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        job = exportJobRepository.save(job);
        Long exportJobId = job.getId();

        try {
            ExportContext context = fetchData(request.getSource(), request);
            String fileName = generateFileName(request.getSource(), format);

            ExportStrategy strategy = selectStrategy(format);
            byte[] data = strategy.export(
                    request.getSource(),
                    context.headers,
                    context.rows
            );

            Path filePath = exportPath.resolve(fileName);
            Files.write(filePath, data);

            job.setFileName(fileName);
            job.setFilePath(filePath.toString());
            job.setFileSize((long) data.length);
            job.setStatus("COMPLETED");
            job.setCompletedAt(LocalDateTime.now());

            log.info("Export completed: jobId={}, source={}, format={}, userId={}",
                    exportJobId, request.getSource(), request.getFormat(), userId);
        } catch (Exception e) {
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());

            log.error("Export failed: jobId={}, source={}, format={}, userId={}, error={}",
                    exportJobId, request.getSource(), request.getFormat(), userId, e.getMessage());
        }

        job = exportJobRepository.save(job);
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public PageResponse<ExportJobResponse> getExportHistory(int page, int size) {
        Long userId = currentUserId();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        return PageResponse.from(exportJobRepository.findByCreatedByOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ExportJobResponse getExportJob(Long id) {
        ExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found: " + id));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public Resource downloadExport(Long id) throws MalformedURLException {
        ExportJob job = exportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Export job not found: " + id));

        if (!"COMPLETED".equals(job.getStatus()) || job.getFilePath() == null) {
            throw new ResourceNotFoundException("Export file not available");
        }

        Path filePath = Paths.get(job.getFilePath()).normalize();
        if (!filePath.startsWith(exportPath)) {
            throw new SecurityException("Invalid file path");
        }

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("Export file not found on disk");
        }
        return resource;
    }

    private ExportContext fetchData(String source, ExportRequest request) {
        return switch (source.toUpperCase()) {
            case "PROCESS_MONITORING" -> fetchReportData("process_monitoring_reports", request);
            case "CHEMICAL_CONSUMPTION" -> fetchReportData("chemical_consumption_reports", request);
            case "DAILY_STARTUP" -> fetchReportData("daily_startup_reports", request);
            case "FIRST_PIECE_INSPECTION" -> fetchReportData("first_piece_inspection_reports", request);
            case "DAILY_INSPECTION" -> fetchReportData("daily_inspection_reports", request);
            case "PDI" -> fetchReportData("pre_delivery_inspection_reports", request);
            case "AUDIT_LOGS" -> fetchAuditLogs(request);
            case "NOTIFICATIONS" -> fetchNotifications(request);
            default -> throw new RuntimeException("Unsupported export source: " + source);
        };
    }

    private ExportContext fetchReportData(String tableName, ExportRequest request) {
        StringBuilder sql = new StringBuilder(
                "SELECT r.report_number, r.report_date::text, r.status, " +
                "COALESCE(s.name,'') as shift, COALESCE(l.name,'') as line, " +
                "COALESCE(CONCAT(u.first_name,' ',u.last_name),'') as created_by, " +
                "COALESCE(r.remarks,'') as remarks " +
                "FROM " + tableName + " r " +
                "LEFT JOIN shifts s ON s.id = r.shift_id " +
                "LEFT JOIN line_master l ON l.id = r.line_id " +
                "LEFT JOIN users u ON u.id = r.created_by " +
                "WHERE 1=1");

        if (request.getDateFrom() != null) sql.append(" AND r.report_date >= :dateFrom");
        if (request.getDateTo() != null) sql.append(" AND r.report_date <= :dateTo");
        if (request.getStatus() != null && !request.getStatus().isBlank())
            sql.append(" AND r.status = :status");
        if (request.getShiftId() != null) sql.append(" AND r.shift_id = :shiftId");
        if (request.getLineId() != null) sql.append(" AND r.line_id = :lineId");

        sql.append(" ORDER BY r.report_date DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        setParams(query, request);
        @SuppressWarnings("unchecked") List<Object[]> rows = query.getResultList();

        String[] headers = {"Report Number", "Date", "Status", "Shift", "Line", "Created By", "Remarks"};
        List<String[]> data = new ArrayList<>();
        for (Object[] row : rows) {
            data.add(new String[]{
                    str(row[0]), str(row[1]), str(row[2]),
                    str(row[3]), str(row[4]), str(row[5]), str(row[6])
            });
        }
        return new ExportContext(headers, data);
    }

    private ExportContext fetchAuditLogs(ExportRequest request) {
        StringBuilder sql = new StringBuilder(
                "SELECT a.timestamp::text, a.employee_id, a.username, a.module::text, " +
                "a.action::text, a.entity_type, a.entity_id " +
                "FROM audit_logs a WHERE 1=1");
        if (request.getDateFrom() != null) sql.append(" AND a.timestamp >= :dateFrom");
        if (request.getDateTo() != null) sql.append(" AND a.timestamp <= :dateTo");
        sql.append(" ORDER BY a.timestamp DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        setParams(query, request);
        @SuppressWarnings("unchecked") List<Object[]> rows = query.getResultList();

        String[] headers = {"Timestamp", "Employee ID", "Username", "Module", "Action", "Entity Type", "Entity ID"};
        List<String[]> data = new ArrayList<>();
        for (Object[] row : rows) {
            data.add(new String[]{str(row[0]), str(row[1]), str(row[2]),
                    str(row[3]), str(row[4]), str(row[5]), str(row[6])});
        }
        return new ExportContext(headers, data);
    }

    private ExportContext fetchNotifications(ExportRequest request) {
        Long userId = currentUserId();
        StringBuilder sql = new StringBuilder(
                "SELECT n.title, n.message, n.type::text, n.priority::text, " +
                "n.is_read::text, n.created_at::text " +
                "FROM notifications n WHERE n.recipient_user_id = :userId");
        if (request.getDateFrom() != null) sql.append(" AND n.created_at >= :dateFrom");
        if (request.getDateTo() != null) sql.append(" AND n.created_at <= :dateTo");
        sql.append(" ORDER BY n.created_at DESC");

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("userId", userId);
        setParams(query, request);
        @SuppressWarnings("unchecked") List<Object[]> rows = query.getResultList();

        String[] headers = {"Title", "Message", "Type", "Priority", "Read", "Created At"};
        List<String[]> data = new ArrayList<>();
        for (Object[] row : rows) {
            data.add(new String[]{str(row[0]), str(row[1]), str(row[2]),
                    str(row[3]), str(row[4]), str(row[5])});
        }
        return new ExportContext(headers, data);
    }

    private ExportStrategy selectStrategy(ExportFormat format) {
        return strategies.stream()
                .filter(s -> s.getFormat() == format)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No strategy found for format: " + format));
    }

    private ExportFormat parseFormat(String format) {
        try {
            return ExportFormat.valueOf(format.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Unsupported export format: " + format);
        }
    }

    private String generateFileName(String source, ExportFormat format) {
        String ext = switch (format) {
            case PDF -> ".pdf";
            case EXCEL -> ".xlsx";
            case CSV -> ".csv";
        };
        return source.toLowerCase() + "_" + UUID.randomUUID() + ext;
    }

    private String buildFiltersJson(ExportRequest request) {
        return "{"
                + "\"source\":\"" + safeJson(request.getSource()) + "\""
                + ",\"format\":\"" + safeJson(request.getFormat()) + "\""
                + (request.getReportType() != null ? ",\"reportType\":\"" + safeJson(request.getReportType()) + "\"" : "")
                + (request.getStatus() != null ? ",\"status\":\"" + safeJson(request.getStatus()) + "\"" : "")
                + (request.getKeyword() != null ? ",\"keyword\":\"" + safeJson(request.getKeyword()) + "\"" : "")
                + "}";
    }

    private String safeJson(String value) {
        return value != null ? value.replace("\"", "\\\"") : "";
    }

    private void setParams(Query query, ExportRequest request) {
        if (request.getDateFrom() != null) query.setParameter("dateFrom", request.getDateFrom());
        if (request.getDateTo() != null) query.setParameter("dateTo", request.getDateTo());
        if (request.getStatus() != null && !request.getStatus().isBlank())
            query.setParameter("status", request.getStatus());
        if (request.getShiftId() != null) query.setParameter("shiftId", request.getShiftId());
        if (request.getLineId() != null) query.setParameter("lineId", request.getLineId());
    }

    private String str(Object o) {
        return o != null ? o.toString() : "";
    }

    private ExportJobResponse toResponse(ExportJob job) {
        return ExportJobResponse.builder()
                .id(job.getId())
                .source(job.getSource())
                .format(job.getFormat().name())
                .filters(job.getFilters())
                .status(job.getStatus())
                .fileName(job.getFileName())
                .fileSize(job.getFileSize())
                .createdAt(job.getCreatedAt())
                .completedAt(job.getCompletedAt())
                .errorMessage(job.getErrorMessage())
                .downloadable("COMPLETED".equals(job.getStatus()))
                .build();
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResourceNotFoundException("User not found");
        }
        return userRepository.findByEmployeeId(auth.getName())
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private record ExportContext(String[] headers, List<String[]> rows) {}

}
