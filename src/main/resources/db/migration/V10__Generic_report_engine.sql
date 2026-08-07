-- ===============================================================
-- V10: Generic Report Engine (Phase 3)
-- Configuration-driven report execution on top of the module
-- hierarchy. Additive only: legacy ReportType engine untouched.
--
-- Hierarchy:
--   report_session -> recorded_process -> recorded_value
--   report_session --save & submit--> report (completed report)
-- ===============================================================

-- ---------------------------------------------------------------
-- Report Session (work in progress; freezes the template version)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS report_session (
    id                      BIGSERIAL    PRIMARY KEY,
    module_id               BIGINT       NOT NULL,
    template_version_id     BIGINT       NOT NULL,
    current_process_id      BIGINT,
    started_at              TIMESTAMP    NOT NULL,
    completed_process_count INTEGER      NOT NULL DEFAULT 0,
    submitted_at            TIMESTAMP,
    status                  VARCHAR(20)  NOT NULL,
    created_by              BIGINT       NOT NULL,
    created_at              TIMESTAMP    NOT NULL,
    updated_at              TIMESTAMP    NOT NULL,
    CONSTRAINT fk_report_session_module FOREIGN KEY (module_id)
        REFERENCES module(id),
    CONSTRAINT fk_report_session_template_version FOREIGN KEY (template_version_id)
        REFERENCES module_template_version(id),
    CONSTRAINT fk_report_session_current_process FOREIGN KEY (current_process_id)
        REFERENCES module_process(id),
    CONSTRAINT fk_report_session_created_by FOREIGN KEY (created_by)
        REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_report_session_module ON report_session(module_id);
CREATE INDEX IF NOT EXISTS idx_report_session_status ON report_session(status);

-- ---------------------------------------------------------------
-- Recorded Process (a template process recorded in a session)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS recorded_process (
    id                     BIGSERIAL    PRIMARY KEY,
    session_id             BIGINT       NOT NULL,
    process_id             BIGINT       NOT NULL,
    process_order_snapshot INTEGER      NOT NULL,
    status                 VARCHAR(20)  NOT NULL,
    completed_at           TIMESTAMP,
    created_at             TIMESTAMP    NOT NULL,
    updated_at             TIMESTAMP    NOT NULL,
    CONSTRAINT fk_recorded_process_session FOREIGN KEY (session_id)
        REFERENCES report_session(id),
    CONSTRAINT fk_recorded_process_process FOREIGN KEY (process_id)
        REFERENCES module_process(id),
    CONSTRAINT uk_recorded_process_session_process UNIQUE (session_id, process_id)
);

CREATE INDEX IF NOT EXISTS idx_recorded_process_session ON recorded_process(session_id);
CREATE INDEX IF NOT EXISTS idx_recorded_process_process ON recorded_process(process_id);

-- ---------------------------------------------------------------
-- Recorded Value (one per process parameter, grouped by process)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS recorded_value (
    id                   BIGSERIAL    PRIMARY KEY,
    recorded_process_id  BIGINT       NOT NULL,
    process_parameter_id BIGINT       NOT NULL,
    parameter_id         BIGINT       NOT NULL,
    observed_value       VARCHAR(1000),
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT fk_recorded_value_recorded_process FOREIGN KEY (recorded_process_id)
        REFERENCES recorded_process(id),
    CONSTRAINT fk_recorded_value_process_parameter FOREIGN KEY (process_parameter_id)
        REFERENCES process_parameter(id),
    CONSTRAINT fk_recorded_value_parameter FOREIGN KEY (parameter_id)
        REFERENCES parameter(id),
    CONSTRAINT uk_recorded_value_process_parameter UNIQUE (recorded_process_id, process_parameter_id)
);

CREATE INDEX IF NOT EXISTS idx_recorded_value_recorded_process ON recorded_value(recorded_process_id);
CREATE INDEX IF NOT EXISTS idx_recorded_value_process_parameter ON recorded_value(process_parameter_id);

-- ---------------------------------------------------------------
-- Completed Report (created on Save & Submit; reflects frozen version)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS report (
    id                 BIGSERIAL    PRIMARY KEY,
    report_number      VARCHAR(40)  NOT NULL,
    module_id          BIGINT       NOT NULL,
    template_version_id BIGINT      NOT NULL,
    started_at         TIMESTAMP    NOT NULL,
    submitted_at       TIMESTAMP    NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    created_by         BIGINT       NOT NULL,
    session_id         BIGINT       NOT NULL,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
    CONSTRAINT uk_generic_report_number UNIQUE (report_number),
    CONSTRAINT fk_generic_report_module FOREIGN KEY (module_id)
        REFERENCES module(id),
    CONSTRAINT fk_generic_report_template_version FOREIGN KEY (template_version_id)
        REFERENCES module_template_version(id),
    CONSTRAINT fk_generic_report_created_by FOREIGN KEY (created_by)
        REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_generic_report_module ON report(module_id);
CREATE INDEX IF NOT EXISTS idx_generic_report_template_version ON report(template_version_id);
CREATE INDEX IF NOT EXISTS idx_generic_report_created_by ON report(created_by);