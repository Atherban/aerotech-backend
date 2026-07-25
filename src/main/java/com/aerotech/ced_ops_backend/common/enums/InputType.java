package com.aerotech.ced_ops_backend.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Type of input field for parameter entry")
public enum InputType {

    NUMBER, // Numeric input
    TEXT, // Text input
    BOOLEAN, // Boolean toggle input
    DROPDOWN // Dropdown selection input

}