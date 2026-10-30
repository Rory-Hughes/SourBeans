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
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL,
  description TEXT         NULL,
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS issues (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  project_id BIGINT       NOT NULL,
  title      VARCHAR(200) NOT NULL,
  status     VARCHAR(50)  NOT NULL,  -- enum constant name, e.g. IN_PROGRESS
  priority   VARCHAR(50)  NOT NULL,  -- enum constant name, e.g. HIGH
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_issues_project
    FOREIGN KEY (project_id) REFERENCES projects (id)
    ON DELETE CASCADE, -- Deleting a project deletes its issues
  INDEX idx_issues_project_status (project_id, status)
) ENGINE=InnoDB;
