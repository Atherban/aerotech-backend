-- ===============================================================
-- V8: Remove backend export feature.
-- Export (PDF/Excel/CSV/Print) is implemented by the frontend; the
-- backend serves structured JSON only. Drop the export job tracking table.
-- ===============================================================

DROP TABLE IF EXISTS export_jobs;
