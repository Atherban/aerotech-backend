-- ===============================================================
-- V12: Remove the legacy ReportType architecture (Phase 5)
-- The Generic Report Engine (report_session / recorded_process /
-- recorded_value / report) fully replaces the six hardcoded report
-- types. Drop the legacy per-type report tables and the legacy
-- report-type-based parameter catalog. The module architecture's
-- `parameter` / `process_parameter` tables are unaffected.
-- */
-- Entry tables are dropped first (FKs to the report tables and
-- parameter_master), then the report tables, then parameter_master.
-- ===============================================================

-- Process Monitoring
DROP TABLE IF EXISTS process_monitoring_entries;
DROP TABLE IF EXISTS process_monitoring_reports;

-- Chemical Consumption
DROP TABLE IF EXISTS chemical_consumption_entries;
DROP TABLE IF EXISTS chemical_consumption_reports;

-- Daily Inspection
DROP TABLE IF EXISTS daily_inspection_entries;
DROP TABLE IF EXISTS daily_inspection_reports;

-- Daily Startup
DROP TABLE IF EXISTS daily_startup_entries;
DROP TABLE IF EXISTS daily_startup_reports;

-- First Piece Inspection
DROP TABLE IF EXISTS first_piece_inspection_entries;
DROP TABLE IF EXISTS first_piece_inspection_reports;

-- Pre-Delivery Inspection
DROP TABLE IF EXISTS pre_delivery_inspection_entries;
DROP TABLE IF EXISTS pre_delivery_inspection_reports;

-- Legacy report-type-based parameter catalog (superseded by the
-- module architecture's global `parameter` table)
DROP TABLE IF EXISTS parameter_master;