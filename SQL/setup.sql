-- SourBeans one-time setup (see ADR-0002, Decision 4)
--
-- Run once per machine by a MySQL user with admin rights:
--   mysql -u root -p < setup.sql
--
-- Creates both databases with an explicit charset and collation so they are
-- identical on every machine, and gives the app user privileges on those two
-- databases only. The app user cannot create databases. Tables are created by
-- the app at startup from schema.sql.
--
-- Before running: replace 'change-me' with the password you will also put in
-- local.properties. Never commit a real password.
--
-- To rebuild from scratch (Decision 11), drop both databases first:
--   DROP DATABASE IF EXISTS sourbeans;
--   DROP DATABASE IF EXISTS sourbeans_test;

CREATE DATABASE IF NOT EXISTS sourbeans
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS sourbeans_test
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- IF NOT EXISTS means an existing user keeps its current password.
CREATE USER IF NOT EXISTS 'sourbeans_app'@'localhost' IDENTIFIED BY 'change-me';

-- Table-level privileges only: schema.sql runs at startup, so the app needs
-- CREATE, ALTER, INDEX and REFERENCES as well as normal data access.
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON sourbeans.* TO 'sourbeans_app'@'localhost';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON sourbeans_test.* TO 'sourbeans_app'@'localhost';
