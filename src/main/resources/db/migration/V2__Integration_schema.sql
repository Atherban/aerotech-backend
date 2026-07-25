CREATE TABLE integrations (
    id               BIGSERIAL   PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    type             VARCHAR(50)  NOT NULL,
    status           VARCHAR(50)  NOT NULL DEFAULT 'INACTIVE',
    config_json      TEXT,
    retry_count      INTEGER      NOT NULL DEFAULT 3,
    timeout_seconds  INTEGER      NOT NULL DEFAULT 30,
    is_active        BOOLEAN      NOT NULL DEFAULT TRUE,
    last_tested_at   TIMESTAMP,
    last_test_status VARCHAR(50),
    created_by       VARCHAR(30),
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_integrations_type ON integrations(type);
CREATE INDEX idx_integrations_status ON integrations(status);
CREATE INDEX idx_integrations_is_active ON integrations(is_active);

CREATE TABLE integration_execution_histories (
    id               BIGSERIAL   PRIMARY KEY,
    integration_id   BIGINT      NOT NULL REFERENCES integrations(id),
    integration_name VARCHAR(200),
    integration_type VARCHAR(50),
    start_time       TIMESTAMP   NOT NULL,
    end_time         TIMESTAMP,
    duration_ms      BIGINT,
    status           VARCHAR(50) NOT NULL,
    error_message    TEXT,
    retry_count      INTEGER     NOT NULL DEFAULT 0,
    response_code    VARCHAR(50),
    trigger_type     VARCHAR(20) NOT NULL DEFAULT 'MANUAL',
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_integration_histories_integration ON integration_execution_histories(integration_id);
CREATE INDEX idx_integration_histories_status ON integration_execution_histories(status);
CREATE INDEX idx_integration_histories_created ON integration_execution_histories(created_at DESC);
