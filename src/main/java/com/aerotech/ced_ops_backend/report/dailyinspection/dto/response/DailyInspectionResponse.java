package com.aerotech.ced_ops_backend.report.dailyinspection.dto.response;

import com.aerotech.ced_ops_backend.common.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyInspectionResponse {

    private Long id;

    private String reportNumber;

    private LocalDate reportDate;

    private String shift;

    private String line;

    private String process;

    private String inspectorName;

    private String correctiveAction;

    private String createdBy;

    private String approvedBy;

    private ReportStatus status;

    private String remarks;

    private LocalDateTime approvedAt;

    private LocalDateTime createdAt;

    private List<DailyInspectionEntryResponse> entries;

}
