-- Master data tables

CREATE TABLE line_master (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(100) NOT NULL UNIQUE,
    description   VARCHAR(300),
    display_order INTEGER      NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE TABLE shifts (
    id         BIGSERIAL   PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    active     BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP   NOT NULL,
    updated_at TIMESTAMP   NOT NULL
);

CREATE TABLE process_master (
    id            BIGSERIAL    PRIMARY KEY,
    name          VARCHAR(150) NOT NULL UNIQUE,
    description   VARCHAR(300),
    display_order INTEGER      NOT NULL,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE TABLE parameter_master (
    id              BIGSERIAL PRIMARY KEY,
    process_id      BIGINT       NOT NULL REFERENCES process_master(id),
    parameter_name  VARCHAR(150) NOT NULL,
    min_value       NUMERIC(10,2),
    max_value       NUMERIC(10,2),
    unit            VARCHAR(30),
    test_method     VARCHAR(150),
    frequency       VARCHAR(255) NOT NULL,
    input_type      VARCHAR(255) NOT NULL,
    mandatory       BOOLEAN      NOT NULL DEFAULT TRUE,
    display_order   INTEGER      NOT NULL,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL,
    updated_at      TIMESTAMP    NOT NULL
);

CREATE INDEX idx_parameter_master_process_id ON parameter_master(process_id);
CREATE INDEX idx_parameter_master_active ON parameter_master(active);
