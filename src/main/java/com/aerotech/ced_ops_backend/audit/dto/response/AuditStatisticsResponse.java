package com.aerotech.ced_ops_backend.audit.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing audit log statistics")
public class AuditStatisticsResponse {

    @Schema(description = "Total number of audit logs", example = "1500")
    private long totalLogs;

    @Schema(description = "Number of logs created today", example = "42")
    private long todayCount;

    @Schema(description = "Logs grouped by module")
    private List<ModuleCount> logsByModule;

    @Schema(description = "Logs grouped by action")
    private List<ActionCount> logsByAction;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Count of logs for a specific module")
    public static class ModuleCount {
        @Schema(description = "Module name", example = "quality")
        private String module;

        @Schema(description = "Number of logs", example = "500")
        private long count;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Count of logs for a specific action")
    public static class ActionCount {
        @Schema(description = "Action name", example = "CREATE")
        private String action;

        @Schema(description = "Number of logs", example = "300")
        private long count;
    }

}
