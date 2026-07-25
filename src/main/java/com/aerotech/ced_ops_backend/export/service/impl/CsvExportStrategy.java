package com.aerotech.ced_ops_backend.export.service.impl;

import com.aerotech.ced_ops_backend.common.enums.ExportFormat;
import com.aerotech.ced_ops_backend.export.service.ExportStrategy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class CsvExportStrategy implements ExportStrategy {

    @Override
    public byte[] export(String title, String[] headers, List<String[]> rows) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {
            if (title != null && !title.isBlank()) {
                writer.write("# ");
                writer.write(title);
                writer.write("\n");
            }

            for (int i = 0; i < headers.length; i++) {
                if (i > 0) writer.write(",");
                writer.write(escapeCsv(headers[i]));
            }
            writer.write("\n");

            for (String[] row : rows) {
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) writer.write(",");
                    writer.write(escapeCsv(row[i] != null ? row[i] : ""));
                }
                writer.write("\n");
            }

            writer.flush();
        }
        return baos.toByteArray();
    }

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.CSV;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

}
