CREATE TABLE system_settings (
    id            BIGSERIAL    PRIMARY KEY,
    setting_key   VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(2000) NOT NULL,
    category      VARCHAR(50)  NOT NULL,
    data_type     VARCHAR(20)  NOT NULL,
    description   VARCHAR(500),
    is_active     BOOLEAN      NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_system_settings_category ON system_settings(category);

CREATE TABLE notifications (
    id                 BIGSERIAL    PRIMARY KEY,
    recipient_user_id  BIGINT       NOT NULL,
    title              VARCHAR(200) NOT NULL,
    message            VARCHAR(2000) NOT NULL,
    type               VARCHAR(50)  NOT NULL,
    related_module     VARCHAR(50),
    related_entity_id  VARCHAR(100),
    priority           VARCHAR(20)  NOT NULL,
    is_read            BOOLEAN      NOT NULL,
    read_at            TIMESTAMP,
    created_at         TIMESTAMP    NOT NULL,
    metadata           TEXT
);

CREATE INDEX idx_notifications_recipient ON notifications(recipient_user_id, is_read);
CREATE INDEX idx_notifications_type ON notifications(recipient_user_id, type);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);

CREATE TABLE export_jobs (
    id            BIGSERIAL    PRIMARY KEY,
    source        VARCHAR(50)  NOT NULL,
    format        VARCHAR(20)  NOT NULL,
    filters       TEXT,
    status        VARCHAR(30)  NOT NULL,
    file_name     VARCHAR(500),
    file_path     VARCHAR(1000),
    file_size     BIGINT,
    created_by    BIGINT       NOT NULL,
    created_at    TIMESTAMP    NOT NULL,
    completed_at  TIMESTAMP,
    error_message TEXT
);

CREATE INDEX idx_export_jobs_created_by ON export_jobs(created_by);
CREATE INDEX idx_export_jobs_created_at ON export_jobs(created_at);

CREATE TABLE attachments (
    id                  BIGSERIAL    PRIMARY KEY,
    original_file_name  VARCHAR(500) NOT NULL,
    stored_file_name    VARCHAR(500) NOT NULL UNIQUE,
    file_extension      VARCHAR(20),
    mime_type           VARCHAR(100),
    file_size           BIGINT,
    storage_path        VARCHAR(1000) NOT NULL,
    file_hash           VARCHAR(64)  UNIQUE,
    uploaded_by         BIGINT       NOT NULL REFERENCES users(id),
    uploaded_at         TIMESTAMP    NOT NULL,
    related_module      VARCHAR(50),
    related_entity_id   VARCHAR(100),
    attachment_category VARCHAR(50),
    description         VARCHAR(500),
    is_active           BOOLEAN      NOT NULL
);

CREATE INDEX idx_attachments_uploaded_by ON attachments(uploaded_by);
CREATE INDEX idx_attachments_entity ON attachments(related_module, related_entity_id);
CREATE INDEX idx_attachments_is_active ON attachments(is_active);

CREATE TABLE audit_logs (
    id            BIGSERIAL    PRIMARY KEY,
    timestamp     TIMESTAMP    NOT NULL,
    user_id       BIGINT,
    employee_id   VARCHAR(30)  NOT NULL,
    username      VARCHAR(100) NOT NULL,
    user_role     VARCHAR(50),
    module        VARCHAR(50)  NOT NULL,
    entity_type   VARCHAR(100),
    entity_id     VARCHAR(100),
    action        VARCHAR(50)  NOT NULL,
    previous_value TEXT,
    new_value     TEXT,
    ip_address    VARCHAR(45),
    user_agent    VARCHAR(500),
    metadata      TEXT
);

CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_module ON audit_logs(module);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
