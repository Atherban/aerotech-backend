CREATE TABLE roles (
    id          BIGSERIAL   PRIMARY KEY,
    name        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL
);

CREATE TABLE users (
    id            BIGSERIAL   PRIMARY KEY,
    employee_id   VARCHAR(30) NOT NULL UNIQUE,
    first_name    VARCHAR(30) NOT NULL,
    last_name     VARCHAR(30) NOT NULL,
    mobile_number VARCHAR(10) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    active        BOOLEAN     NOT NULL DEFAULT TRUE,
    role_id       BIGINT      NOT NULL REFERENCES roles(id),
    created_at    TIMESTAMP   NOT NULL,
    updated_at    TIMESTAMP   NOT NULL
);

CREATE INDEX idx_users_role_id ON users(role_id);

CREATE TABLE refresh_token (
    id          BIGSERIAL   PRIMARY KEY,
    token       VARCHAR(512) NOT NULL UNIQUE,
    expiry_date TIMESTAMP    NOT NULL,
    user_id     BIGINT       REFERENCES users(id),
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_token ON refresh_token(token);
