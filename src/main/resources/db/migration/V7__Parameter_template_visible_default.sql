-- ===============================================================
-- V7: Report Template Configuration — parameter visibility & default value.
-- Adds the remaining template attributes to parameter_master:
--   visible       -> whether the parameter is shown in the report entry form
--   default_value -> value pre-filled when the parameter is rendered
-- ===============================================================

ALTER TABLE parameter_master ADD COLUMN IF NOT EXISTS visible BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE parameter_master ADD COLUMN IF NOT EXISTS default_value VARCHAR(255);
