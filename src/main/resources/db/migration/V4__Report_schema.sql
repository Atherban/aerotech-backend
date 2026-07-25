-- ===============================================================
-- Report tables — each inherits the same base structure via
-- BaseReport (report_number, report_type, report_date, shift_id,
-- line_id, status, created_by, approved_by, approved_at, remarks)
-- + created_at, updated_at from BaseEntity.
-- ===============================================================

CREATE TABLE process_monitoring_reports (
    id            BIGSERIAL    PRIMARY KEY,
    report_number VARCHAR(255) NOT NULL UNIQUE,
    report_type   VARCHAR(255) NOT NULL,
    report_date   DATE         NOT NULL,
    shift_id      BIGINT       NOT NULL REFERENCES shifts(id),
    line_id       BIGINT       NOT NULL REFERENCES line_master(id),
    status        VARCHAR(255) NOT NULL,
    created_by    BIGINT       NOT NULL REFERENCES users(id),
    approved_by   BIGINT       REFERENCES users(id),
    approved_at   TIMESTAMP,
    remarks       VARCHAR(1000),
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_pmr_shift_id ON process_monitoring_reports(shift_id);
CREATE INDEX idx_pmr_line_id ON process_monitoring_reports(line_id);
CREATE INDEX idx_pmr_created_by ON process_monitoring_reports(created_by);
CREATE INDEX idx_pmr_status ON process_monitoring_reports(status);
CREATE INDEX idx_pmr_report_date ON process_monitoring_reports(report_date);

CREATE TABLE process_monitoring_entries (
    id                BIGSERIAL   PRIMARY KEY,
    report_id         BIGINT      NOT NULL REFERENCES process_monitoring_reports(id),
    parameter_id      BIGINT      NOT NULL REFERENCES parameter_master(id),
    observed_value    VARCHAR(200) NOT NULL,
    inspection_result VARCHAR(255) NOT NULL,
    remark            VARCHAR(500),
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_pme_report_id ON process_monitoring_entries(report_id);
CREATE INDEX idx_pme_parameter_id ON process_monitoring_entries(parameter_id);

-- ===============================================================
-- Chemical Consumption
-- ===============================================================

CREATE TABLE chemical_consumption_reports (
    id            BIGSERIAL    PRIMARY KEY,
    report_number VARCHAR(255) NOT NULL UNIQUE,
    report_type   VARCHAR(255) NOT NULL,
    report_date   DATE         NOT NULL,
    shift_id      BIGINT       NOT NULL REFERENCES shifts(id),
    line_id       BIGINT       NOT NULL REFERENCES line_master(id),
    status        VARCHAR(255) NOT NULL,
    created_by    BIGINT       NOT NULL REFERENCES users(id),
    approved_by   BIGINT       REFERENCES users(id),
    approved_at   TIMESTAMP,
    remarks       VARCHAR(1000),
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_ccr_shift_id ON chemical_consumption_reports(shift_id);
CREATE INDEX idx_ccr_line_id ON chemical_consumption_reports(line_id);
CREATE INDEX idx_ccr_created_by ON chemical_consumption_reports(created_by);
CREATE INDEX idx_ccr_status ON chemical_consumption_reports(status);
CREATE INDEX idx_ccr_report_date ON chemical_consumption_reports(report_date);

CREATE TABLE chemical_consumption_entries (
    id                BIGSERIAL   PRIMARY KEY,
    report_id         BIGINT      NOT NULL REFERENCES chemical_consumption_reports(id),
    parameter_id      BIGINT      NOT NULL REFERENCES parameter_master(id),
    observed_value    VARCHAR(200) NOT NULL,
    inspection_result VARCHAR(255) NOT NULL,
    remark            VARCHAR(500),
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_cce_report_id ON chemical_consumption_entries(report_id);
CREATE INDEX idx_cce_parameter_id ON chemical_consumption_entries(parameter_id);

-- ===============================================================
-- Daily Inspection
-- ===============================================================

CREATE TABLE daily_inspection_reports (
    id                BIGSERIAL    PRIMARY KEY,
    report_number     VARCHAR(255) NOT NULL UNIQUE,
    report_type       VARCHAR(255) NOT NULL,
    report_date       DATE         NOT NULL,
    shift_id          BIGINT       NOT NULL REFERENCES shifts(id),
    line_id           BIGINT       NOT NULL REFERENCES line_master(id),
    status            VARCHAR(255) NOT NULL,
    created_by        BIGINT       NOT NULL REFERENCES users(id),
    approved_by       BIGINT       REFERENCES users(id),
    approved_at       TIMESTAMP,
    remarks           VARCHAR(1000),
    process_id        BIGINT       NOT NULL REFERENCES process_master(id),
    inspector_name    VARCHAR(100),
    corrective_action VARCHAR(1000),
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL
);

CREATE INDEX idx_dir_shift_id ON daily_inspection_reports(shift_id);
CREATE INDEX idx_dir_line_id ON daily_inspection_reports(line_id);
CREATE INDEX idx_dir_created_by ON daily_inspection_reports(created_by);
CREATE INDEX idx_dir_status ON daily_inspection_reports(status);
CREATE INDEX idx_dir_report_date ON daily_inspection_reports(report_date);
CREATE INDEX idx_dir_process_id ON daily_inspection_reports(process_id);

CREATE TABLE daily_inspection_entries (
    id                BIGSERIAL   PRIMARY KEY,
    report_id         BIGINT      NOT NULL REFERENCES daily_inspection_reports(id),
    parameter_id      BIGINT      NOT NULL REFERENCES parameter_master(id),
    observed_value    VARCHAR(200) NOT NULL,
    inspection_result VARCHAR(255) NOT NULL,
    remark            VARCHAR(500),
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_die_report_id ON daily_inspection_entries(report_id);
CREATE INDEX idx_die_parameter_id ON daily_inspection_entries(parameter_id);

-- ===============================================================
-- Daily Startup
-- ===============================================================

CREATE TABLE daily_startup_reports (
    id            BIGSERIAL    PRIMARY KEY,
    report_number VARCHAR(255) NOT NULL UNIQUE,
    report_type   VARCHAR(255) NOT NULL,
    report_date   DATE         NOT NULL,
    shift_id      BIGINT       NOT NULL REFERENCES shifts(id),
    line_id       BIGINT       NOT NULL REFERENCES line_master(id),
    status        VARCHAR(255) NOT NULL,
    created_by    BIGINT       NOT NULL REFERENCES users(id),
    approved_by   BIGINT       REFERENCES users(id),
    approved_at   TIMESTAMP,
    remarks       VARCHAR(1000),
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_dsr_shift_id ON daily_startup_reports(shift_id);
CREATE INDEX idx_dsr_line_id ON daily_startup_reports(line_id);
CREATE INDEX idx_dsr_created_by ON daily_startup_reports(created_by);
CREATE INDEX idx_dsr_status ON daily_startup_reports(status);
CREATE INDEX idx_dsr_report_date ON daily_startup_reports(report_date);

CREATE TABLE daily_startup_entries (
    id                BIGSERIAL   PRIMARY KEY,
    report_id         BIGINT      NOT NULL REFERENCES daily_startup_reports(id),
    parameter_id      BIGINT      NOT NULL REFERENCES parameter_master(id),
    observed_value    VARCHAR(200) NOT NULL,
    inspection_result VARCHAR(255) NOT NULL,
    remark            VARCHAR(500),
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_dse_report_id ON daily_startup_entries(report_id);
CREATE INDEX idx_dse_parameter_id ON daily_startup_entries(parameter_id);

-- ===============================================================
-- First Piece Inspection
-- ===============================================================

CREATE TABLE first_piece_inspection_reports (
    id                    BIGSERIAL    PRIMARY KEY,
    report_number         VARCHAR(255) NOT NULL UNIQUE,
    report_type           VARCHAR(255) NOT NULL,
    report_date           DATE         NOT NULL,
    shift_id              BIGINT       NOT NULL REFERENCES shifts(id),
    line_id               BIGINT       NOT NULL REFERENCES line_master(id),
    status                VARCHAR(255) NOT NULL,
    created_by            BIGINT       NOT NULL REFERENCES users(id),
    approved_by           BIGINT       REFERENCES users(id),
    approved_at           TIMESTAMP,
    remarks               VARCHAR(1000),
    process_id            BIGINT       NOT NULL REFERENCES process_master(id),
    product_casting_number VARCHAR(100),
    operator_name         VARCHAR(100),
    inspector_name        VARCHAR(100),
    created_at            TIMESTAMP    NOT NULL,
    updated_at            TIMESTAMP    NOT NULL
);

CREATE INDEX idx_fpir_shift_id ON first_piece_inspection_reports(shift_id);
CREATE INDEX idx_fpir_line_id ON first_piece_inspection_reports(line_id);
CREATE INDEX idx_fpir_created_by ON first_piece_inspection_reports(created_by);
CREATE INDEX idx_fpir_status ON first_piece_inspection_reports(status);
CREATE INDEX idx_fpir_report_date ON first_piece_inspection_reports(report_date);
CREATE INDEX idx_fpir_process_id ON first_piece_inspection_reports(process_id);

CREATE TABLE first_piece_inspection_entries (
    id                BIGSERIAL   PRIMARY KEY,
    report_id         BIGINT      NOT NULL REFERENCES first_piece_inspection_reports(id),
    parameter_id      BIGINT      NOT NULL REFERENCES parameter_master(id),
    observed_value    VARCHAR(200) NOT NULL,
    inspection_result VARCHAR(255) NOT NULL,
    remark            VARCHAR(500),
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_fpie_report_id ON first_piece_inspection_entries(report_id);
CREATE INDEX idx_fpie_parameter_id ON first_piece_inspection_entries(parameter_id);

-- ===============================================================
-- Pre-Delivery Inspection
-- ===============================================================

CREATE TABLE pre_delivery_inspection_reports (
    id                  BIGSERIAL    PRIMARY KEY,
    report_number       VARCHAR(255) NOT NULL UNIQUE,
    report_type         VARCHAR(255) NOT NULL,
    report_date         DATE         NOT NULL,
    shift_id            BIGINT       NOT NULL REFERENCES shifts(id),
    line_id             BIGINT       NOT NULL REFERENCES line_master(id),
    status              VARCHAR(255) NOT NULL,
    created_by          BIGINT       NOT NULL REFERENCES users(id),
    approved_by         BIGINT       REFERENCES users(id),
    approved_at         TIMESTAMP,
    remarks             VARCHAR(1000),
    product_part_number VARCHAR(150),
    batch_number        VARCHAR(100),
    inspector_name      VARCHAR(100),
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL
);

CREATE INDEX idx_pdir_shift_id ON pre_delivery_inspection_reports(shift_id);
CREATE INDEX idx_pdir_line_id ON pre_delivery_inspection_reports(line_id);
CREATE INDEX idx_pdir_created_by ON pre_delivery_inspection_reports(created_by);
CREATE INDEX idx_pdir_status ON pre_delivery_inspection_reports(status);
CREATE INDEX idx_pdir_report_date ON pre_delivery_inspection_reports(report_date);

CREATE TABLE pre_delivery_inspection_entries (
    id                BIGSERIAL   PRIMARY KEY,
    report_id         BIGINT      NOT NULL REFERENCES pre_delivery_inspection_reports(id),
    parameter_id      BIGINT      NOT NULL REFERENCES parameter_master(id),
    observed_value    VARCHAR(200) NOT NULL,
    inspection_result VARCHAR(255) NOT NULL,
    remark            VARCHAR(500),
    created_at        TIMESTAMP   NOT NULL,
    updated_at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_pdie_report_id ON pre_delivery_inspection_entries(report_id);
CREATE INDEX idx_pdie_parameter_id ON pre_delivery_inspection_entries(parameter_id);
