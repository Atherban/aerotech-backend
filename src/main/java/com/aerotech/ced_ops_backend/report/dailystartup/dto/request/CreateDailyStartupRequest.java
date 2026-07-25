package com.aerotech.ced_ops_backend.report.dailystartup.dto.request;

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
public class CreateDailyStartupRequest {

    @NotNull(message = "Report date is required")
    private LocalDate reportDate;

    @NotNull(message = "Shift ID is required")
    private Long shiftId;

    @NotNull(message = "Line ID is required")
    private Long lineId;

    @jakarta.validation.constraints.Size(max = 1000, message = "Remarks must not exceed 1000 characters")
    private String remarks;

    @Valid
    @NotEmpty(message = "At least one entry is required")
    private List<DailyStartupEntryRequest> entries;

}
