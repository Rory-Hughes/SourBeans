-- SourBeans schema (see ADR-0002)
--
-- Run by Spring at startup (spring.sql.init.mode=always), so every statement
-- must be safe to re-run: tables use IF NOT EXISTS and seeds use
-- ON DUPLICATE KEY UPDATE. The database itself is created by the JDBC URL
-- (createDatabaseIfNotExist=true), so there is no CREATE DATABASE or USE here.
-- Requires MySQL 8.0.19+ for the "AS seed" alias syntax in the seed inserts.

-- -------------------
-- Lookup tables    --
-- -------------------

CREATE TABLE IF NOT EXISTS statuses (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  code       VARCHAR(50) NOT NULL,                -- stable value used by code, e.g. IN_PROGRESS
  label      VARCHAR(50) NOT NULL,                -- friendly text for the UI, e.g. In progress
  sort_order INT         NOT NULL,
  active     BOOLEAN     NOT NULL DEFAULT TRUE,   -- retire values instead of deleting them
  PRIMARY KEY (id),
  CONSTRAINT uq_statuses_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS priorities (
  id         BIGINT      NOT NULL AUTO_INCREMENT,
  code       VARCHAR(50) NOT NULL,
  label      VARCHAR(50) NOT NULL,
  sort_order INT         NOT NULL,
  active     BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  CONSTRAINT uq_priorities_code UNIQUE (code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS roles (
  id   BIGINT       NOT NULL AUTO_INCREMENT,
  name VARCHAR(50)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_roles_name UNIQUE (name)
) ENGINE=InnoDB;

-- -------------------
-- Core tables      --
-- -------------------

CREATE TABLE IF NOT EXISTS users (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  username      VARCHAR(50)  NOT NULL,
  email         VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,  -- 100% Guarenteed to be "encrypted" :)
  created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uq_users_username UNIQUE (username),
  CONSTRAINT uq_users_email    UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS projects (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  description TEXT         NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS issues (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  project_id  BIGINT       NOT NULL,
  title       VARCHAR(200) NOT NULL,
  status_id   BIGINT       NOT NULL,
  priority_id BIGINT       NOT NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_issues_project
    FOREIGN KEY (project_id) REFERENCES projects (id)
    ON DELETE CASCADE, -- Deleting a project deletes its issues
  CONSTRAINT fk_issues_status
    FOREIGN KEY (status_id) REFERENCES statuses (id)
    ON DELETE RESTRICT,
  CONSTRAINT fk_issues_priority
    FOREIGN KEY (priority_id) REFERENCES priorities (id)
    ON DELETE RESTRICT,
  INDEX idx_issues_project_status (project_id, status_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS project_members (
  project_id BIGINT NOT NULL,
  user_id    BIGINT NOT NULL,
  role_id    BIGINT NOT NULL,
  PRIMARY KEY (project_id, user_id),
  CONSTRAINT fk_pm_project
    FOREIGN KEY (project_id) REFERENCES projects (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_pm_user
    FOREIGN KEY (user_id) REFERENCES users (id)
    ON DELETE CASCADE,
  CONSTRAINT fk_pm_role
    FOREIGN KEY (role_id) REFERENCES roles (id)
    ON DELETE RESTRICT,
  INDEX idx_pm_user (user_id)
) ENGINE=InnoDB;

-- -------------------
-- Seed data        --
-- -------------------
-- This file is the single source of truth for lookup rows. Every start
-- re-asserts these values, so manual edits to them are overwritten.
-- Code refers to rows by code (or name for roles), never by numeric id.

INSERT INTO statuses (code, label, sort_order, active) VALUES
  ('TODO',        'To do',       1, TRUE),
  ('IN_PROGRESS', 'In progress', 2, TRUE),
  ('DONE',        'Done',        3, TRUE)
AS seed
ON DUPLICATE KEY UPDATE
  label      = seed.label,
  sort_order = seed.sort_order,
  active     = seed.active;

INSERT INTO priorities (code, label, sort_order, active) VALUES
  ('UNDEFINED', 'Undefined', 1, TRUE),
  ('LOW',       'Low',       2, TRUE),
  ('MEDIUM',    'Medium',    3, TRUE),
  ('HIGH',      'High',      4, TRUE)
AS seed
ON DUPLICATE KEY UPDATE
  label      = seed.label,
  sort_order = seed.sort_order,
  active     = seed.active;

INSERT INTO roles (name) VALUES
  ('OWNER'),
  ('MEMBER')
AS seed
ON DUPLICATE KEY UPDATE
  name = seed.name;
