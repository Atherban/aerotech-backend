package com.aerotech.ced_ops_backend.export.service;

import com.aerotech.ced_ops_backend.common.enums.ExportFormat;

import java.io.IOException;
import java.util.List;

public interface ExportStrategy {

    byte[] export(String title, String[] headers, List<String[]> rows) throws IOException;

    ExportFormat getFormat();

}
