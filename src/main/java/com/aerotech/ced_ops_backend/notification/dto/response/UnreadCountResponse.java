package com.aerotech.ced_ops_backend.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing the unread notification count")
public class UnreadCountResponse {

    @Schema(description = "Number of unread notifications", example = "5", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private long count;

}
