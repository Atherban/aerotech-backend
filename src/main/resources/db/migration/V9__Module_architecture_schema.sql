-- ===============================================================
-- V9: Module-driven report architecture (Phase 1)
-- New configuration-driven hierarchy. Additive only: legacy
-- ReportType tables are left untouched and removed in Phase 5.
--
-- Hierarchy:
--   module_type -> module -> module_template_version -> module_process
--     -> process_parameter -> parameter (global reusable)
-- ===============================================================

-- ---------------------------------------------------------------
-- Module Type (configurable master data)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS module_type (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(300),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT uk_module_type_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_module_type_name ON module_type(name);

-- ---------------------------------------------------------------
-- Module (reusable report Template)
-- lifecycle: DRAFT / ACTIVE / ARCHIVED
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS module (
    id             BIGSERIAL    PRIMARY KEY,
    module_type_id BIGINT       NOT NULL REFERENCES module_type(id),
    name           VARCHAR(150) NOT NULL,
    prefix         VARCHAR(10)  NOT NULL,
    description    VARCHAR(500),
    status         VARCHAR(20)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    CONSTRAINT uk_module_name UNIQUE (name),
    CONSTRAINT uk_module_prefix UNIQUE (prefix)
);

CREATE INDEX IF NOT EXISTS idx_module_module_type_id ON module(module_type_id);
CREATE INDEX IF NOT EXISTS idx_module_status ON module(status);

-- ---------------------------------------------------------------
-- Template Version (versioned together with the module template)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS module_template_version (
    id             BIGSERIAL    PRIMARY KEY,
    module_id      BIGINT       NOT NULL,
    version_number INTEGER      NOT NULL,
    status         VARCHAR(20)  NOT NULL,
    change_note    VARCHAR(500),
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    CONSTRAINT fk_template_version_module FOREIGN KEY (module_id)
        REFERENCES module(id),
    CONSTRAINT uk_template_version_module_number UNIQUE (module_id, version_number)
);

CREATE INDEX IF NOT EXISTS idx_template_version_module ON module_template_version(module_id);

-- ---------------------------------------------------------------
-- Process (belongs to a template version; ordered by display_order)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS module_process (
    id                 BIGSERIAL    PRIMARY KEY,
    template_version_id BIGINT       NOT NULL,
    name               VARCHAR(150) NOT NULL,
    description        VARCHAR(500),
    display_order      INTEGER      NOT NULL,
    status             VARCHAR(20)  NOT NULL,
    created_at         TIMESTAMP    NOT NULL,
    updated_at         TIMESTAMP    NOT NULL,
    CONSTRAINT fk_process_template_version FOREIGN KEY (template_version_id)
        REFERENCES module_template_version(id),
    CONSTRAINT uk_process_template_name UNIQUE (template_version_id, name)
);

CREATE INDEX IF NOT EXISTS idx_process_template_version ON module_process(template_version_id);

-- ---------------------------------------------------------------
-- Parameter (global reusable definition; exists exactly once)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS parameter (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    input_type  VARCHAR(30)  NOT NULL,
    description VARCHAR(500),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT uk_parameter_name UNIQUE (name)
);

-- ---------------------------------------------------------------
-- ProcessParameter (per-process specification of a Parameter)
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS process_parameter (
    id              BIGSERIAL   PRIMARY KEY,
    process_id      BIGINT      NOT NULL,
    parameter_id    BIGINT      NOT NULL,
    display_order   INTEGER     NOT NULL,
    mandatory       BOOLEAN     NOT NULL DEFAULT TRUE,
    visible         BOOLEAN     NOT NULL DEFAULT TRUE,
    default_value   VARCHAR(255),
    unit            VARCHAR(30),
    minimum_value   NUMERIC(10,2),
    maximum_value   NUMERIC(10,2),
    active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP   NOT NULL,
    updated_at      TIMESTAMP   NOT NULL,
    CONSTRAINT fk_process_parameter_process FOREIGN KEY (process_id)
        REFERENCES module_process(id),
    CONSTRAINT fk_process_parameter_parameter FOREIGN KEY (parameter_id)
        REFERENCES parameter(id),
    CONSTRAINT uk_process_parameter UNIQUE (process_id, parameter_id)
);

CREATE INDEX IF NOT EXISTS idx_process_parameter_process ON process_parameter(process_id);
CREATE INDEX IF NOT EXISTS idx_process_parameter_parameter ON process_parameter(parameter_id);