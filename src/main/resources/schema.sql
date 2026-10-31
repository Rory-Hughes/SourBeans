-- SourBeans schema (see ADR-0002)
--
-- Run by Spring at startup (spring.sql.init.mode=always), so every statement
-- must be safe to re-run: tables use CREATE TABLE IF NOT EXISTS.
-- The databases are created beforehand by setup.sql, so there is no
-- CREATE DATABASE or USE here. Tables take their charset and collation
-- (utf8mb4 / utf8mb4_unicode_ci) from the database that setup.sql creates.
--
-- Release 1 tables only. users and project_members are added when Release 2
-- starts. There are no lookup tables and no seed data: status and priority
-- are Java enums stored as strings, and the allowed values live in the code.

-- -------------------
-- Core tables      --
-- -------------------

CREATE TABLE IF NOT EXISTS projects (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  name        VARCHAR(80)   NOT NULL,
  description VARCHAR(1000) NULL,
  created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT verify_name_length CHECK (CHAR_LENGTH(TRIM( name )) >= 3)-- Minimum 3 chars trimmed for whitespace
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS issues (
  id          BIGINT        NOT NULL AUTO_INCREMENT,
  project_id  BIGINT        NOT NULL,
  title       VARCHAR(120)  NOT NULL,
  description VARCHAR(5000) NULL,
  status      VARCHAR(50)   NOT NULL DEFAULT 'OPEN',
  priority    VARCHAR(50)   NOT NULL DEFAULT 'MEDIUM',
  created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT verify_title_length CHECK (CHAR_LENGTH(TRIM(title)) >= 5), -- Minimum 5 chars trimmed for whitespace
  CONSTRAINT fk_issues_project
    FOREIGN KEY (project_id) REFERENCES projects (id)
    ON DELETE CASCADE, -- Deleting a project deletes its issues
  INDEX idx_issues_project_status (project_id, status)
) ENGINE=InnoDB;
