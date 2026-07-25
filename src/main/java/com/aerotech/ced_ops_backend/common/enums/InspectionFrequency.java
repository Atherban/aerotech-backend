package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Frequency at which inspections should be performed")
public enum InspectionFrequency {

    HOURLY, // Every hour
    EVERY_2_HOURS, // Every 2 hours
    EVERY_4_HOURS, // Every 4 hours
    EVERY_SHIFT, // Once per shift
    DAILY, // Once per day
    WEEKLY, // Once per week
    MONTHLY, // Once per month
    PER_BATCH // Once per production batch

}