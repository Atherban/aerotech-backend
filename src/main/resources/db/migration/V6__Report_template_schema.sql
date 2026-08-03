-- ===============================================================
-- V6: Remove ProcessMaster concept.
-- Parameters now belong directly to a Report Type.
-- Shifts gain startTime/endTime for automatic shift detection.
-- ===============================================================

-- ---------------------------------------------------------------
-- 1. parameter_master: replace process_id FK with report_type
-- ---------------------------------------------------------------

ALTER TABLE parameter_master ADD COLUMN report_type VARCHAR(255);

-- Backfill: existing parameters have no report-type linkage.
-- Super Admin reconfigures each report type's parameters (Phase 1 setup).
UPDATE parameter_master SET report_type = 'PROCESS_MONITORING'
    WHERE report_type IS NULL;

ALTER TABLE parameter_master ALTER COLUMN report_type SET NOT NULL;

ALTER TABLE parameter_master DROP CONSTRAINT IF EXISTS parameter_master_process_id_fkey;
DROP INDEX IF EXISTS idx_parameter_master_process_id;
ALTER TABLE parameter_master DROP COLUMN IF EXISTS process_id;

CREATE INDEX idx_parameter_master_report_type ON parameter_master(report_type);

-- ---------------------------------------------------------------
-- 2. daily_inspection_reports: drop process_id
-- ---------------------------------------------------------------

ALTER TABLE daily_inspection_reports DROP CONSTRAINT IF EXISTS daily_inspection_reports_process_id_fkey;
DROP INDEX IF EXISTS idx_dir_process_id;
ALTER TABLE daily_inspection_reports DROP COLUMN IF EXISTS process_id;

-- ---------------------------------------------------------------
-- 3. first_piece_inspection_reports: drop process_id
-- ---------------------------------------------------------------

ALTER TABLE first_piece_inspection_reports DROP CONSTRAINT IF EXISTS first_piece_inspection_reports_process_id_fkey;
DROP INDEX IF EXISTS idx_fpir_process_id;
ALTER TABLE first_piece_inspection_reports DROP COLUMN IF EXISTS process_id;

-- ---------------------------------------------------------------
-- 4. Remove ProcessMaster
-- ---------------------------------------------------------------

DROP TABLE IF EXISTS process_master;

-- ---------------------------------------------------------------
-- 5. Shifts: add start_time / end_time for shift detection
-- ---------------------------------------------------------------

ALTER TABLE shifts ADD COLUMN IF NOT EXISTS start_time TIME;
ALTER TABLE shifts ADD COLUMN IF NOT EXISTS end_time TIME;
