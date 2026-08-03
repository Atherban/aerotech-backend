package com.aerotech.ced_ops_backend.common.util;

import com.aerotech.ced_ops_backend.common.enums.ReportType;
import com.aerotech.ced_ops_backend.report.support.ReportTypeMetadata;
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

        String prefix = ReportTypeMetadata.of(reportType).getPrefix();

        return "%s-%s-%05d".formatted(
                prefix,
                LocalDate.now().format(FORMATTER),
                sequence
        );

    }

}