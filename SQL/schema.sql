CREATE DATABASE IF NOT EXISTS sourbeans
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sourbeans;

-- -------------------
-- Lookup tables    --
-- -------------------

CREATE TABLE IF NOT EXISTS statuses (
  id   BIGINT       NOT NULL AUTO_INCREMENT,
  name VARCHAR(50)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_statuses_name UNIQUE (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS priorities (
  id   BIGINT       NOT NULL AUTO_INCREMENT,
  name VARCHAR(50)  NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uq_priorities_name UNIQUE (name)
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

INSERT IGNORE INTO statuses (name) VALUES
  ('TODO'),
  ('IN_PROGRESS'),
  ('DONE');

INSERT IGNORE INTO priorities (name) VALUES
  ('UNDEFINED'),
  ('LOW'),
  ('MEDIUM'),
  ('HIGH');

INSERT IGNORE INTO roles (name) VALUES
  ('OWNER'),
  ('MEMBER');
