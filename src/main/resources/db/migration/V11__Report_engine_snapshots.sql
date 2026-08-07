-- ===============================================================
-- V11: Report Engine snapshots (Phase 4)
-- Business-facing read models (Dashboard, Global Search, Analytics)
-- now read from the Generic Report Engine. Historical reports must
-- remain readable even if master data changes later, so the
-- completed `report` and its `recorded_value`s carry immutable
-- snapshots of the names/specs in use when the report was filled.
-- Additive only: legacy tables untouched.
-- ===============================================================

-- ---------------------------------------------------------------
-- Completed report snapshots
--   - module_name / module_prefix / module_type_name: immutable
--     identities of the module used at submit time
--   - module_type_id: filter support without live master joins
--   - template_version_number: frozen version number
--   - shift_id / shift_name / line_id / line_name: captured at
--     session start; names are snapshotted for display
--   - approved_at / approved_by: forward-compatible approval
--     columns (no approval workflow yet, always NULL)
-- ---------------------------------------------------------------
ALTER TABLE report
    ADD COLUMN IF NOT EXISTS module_name             VARCHAR(150) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS module_prefix           VARCHAR(10)  NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS template_version_number INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS module_type_id          BIGINT,
    ADD COLUMN IF NOT EXISTS module_type_name        VARCHAR(100) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS shift_id                BIGINT,
    ADD COLUMN IF NOT EXISTS shift_name              VARCHAR(50),
    ADD COLUMN IF NOT EXISTS line_id                 BIGINT,
    ADD COLUMN IF NOT EXISTS line_name               VARCHAR(100),
    ADD COLUMN IF NOT EXISTS approved_at             TIMESTAMP,
    ADD COLUMN IF NOT EXISTS approved_by             BIGINT;

CREATE INDEX IF NOT EXISTS idx_generic_report_status ON report(status);
CREATE INDEX IF NOT EXISTS idx_generic_report_started_at ON report(started_at);
CREATE INDEX IF NOT EXISTS idx_generic_report_module_type ON report(module_type_id);
CREATE INDEX IF NOT EXISTS idx_generic_report_shift ON report(shift_id);
CREATE INDEX IF NOT EXISTS idx_generic_report_line ON report(line_id);

-- ---------------------------------------------------------------
-- Report session carries the shift/line captured at start so the
-- completed report can be snapshotted at submit time.
-- ---------------------------------------------------------------
ALTER TABLE report_session
    ADD COLUMN IF NOT EXISTS shift_id   BIGINT,
    ADD COLUMN IF NOT EXISTS shift_name VARCHAR(50),
    ADD COLUMN IF NOT EXISTS line_id    BIGINT,
    ADD COLUMN IF NOT EXISTS line_name  VARCHAR(100);

-- ---------------------------------------------------------------
-- Recorded value snapshots (spec in use when the value was saved)
--   - parameter_name / input_type: identity of the global parameter
--   - unit: display unit from the process parameter
--   - minimum_value / maximum_value: the config-driven spec used to
--     derive PASS/FAIL for analytics; frozen so later spec edits
--     never change historical results
-- ---------------------------------------------------------------
ALTER TABLE recorded_value
    ADD COLUMN IF NOT EXISTS parameter_name VARCHAR(150) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS unit           VARCHAR(30),
    ADD COLUMN IF NOT EXISTS input_type     VARCHAR(30)  NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS minimum_value  NUMERIC(10,2),
    ADD COLUMN IF NOT EXISTS maximum_value  NUMERIC(10,2);
