package com.aerotech.ced_ops_backend.report.dailyinspection.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDailyInspectionRequest {

    @NotNull(message = "Report date is required")
    private LocalDate reportDate;

    @NotNull(message = "Shift ID is required")
    private Long shiftId;

    @NotNull(message = "Line ID is required")
    private Long lineId;

    @NotNull(message = "Process ID is required")
    private Long processId;

    @jakarta.validation.constraints.Size(max = 200, message = "Inspector name must not exceed 200 characters")
    private String inspectorName;

    @jakarta.validation.constraints.Size(max = 1000, message = "Corrective action must not exceed 1000 characters")
    private String correctiveAction;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;

    @Valid
    @NotEmpty(message = "At least one entry is required")
    private List<DailyInspectionEntryRequest> entries;

}
