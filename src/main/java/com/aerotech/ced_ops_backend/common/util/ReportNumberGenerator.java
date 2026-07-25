package com.aerotech.ced_ops_backend.common.util;

import com.aerotech.ced_ops_backend.common.enums.ReportType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class ReportNumberGenerator {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    public String generate(
            ReportType reportType,
            Long sequence
    ) {

        String prefix = switch (reportType) {

            case PROCESS_MONITORING -> "PMR";

            case PDI -> "PDI";

            case DAILY_STARTUP -> "DSR";

            case CHEMICAL_CONSUMPTION -> "CCR";

            case FIRST_PIECE_INSPECTION -> "FPI";

            case DAILY_INSPECTION -> "DIR";

        };

        return "%s-%s-%05d".formatted(
                prefix,
                LocalDate.now().format(FORMATTER),
                sequence
        );

    }

}