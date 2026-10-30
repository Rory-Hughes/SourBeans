# ADR-0002: Database Schema Management, Lookup Tables and Test Data

**Status:** Proposed\
**Date:** 2026-10-30\
**Depends on:** ADR-0001\

## Context

ADR-0001 chose MySQL with Spring Data JPA and a local MySQL installation on each developer's machine. A schema draft already exists, and the charter puts status and priority filters in Release 1 and `OWNER`/`MEMBER` permissions in Release 2.

We need to decide how the schema is created and kept identical on both machines, how fixed reference data is modeled, how tests get a database, and how credentials are shared without being committed. We expect the schema to change at most about two times after the first version, because the requirements are already modeled.

The app should apply the schema for us, and we do not want to lose all local data every time we start a test session.

## Options considered

### 1. Schema management

- **A. Hand-written `schema.sql` run by Spring at startup (chosen).** Written to be safe to re-run with `CREATE TABLE IF NOT EXISTS`, so existing data survives restarts. Plain SQL we control. Changes to existing tables need a manual `ALTER`.
- **B. `schema.sql` run manually outside the app.** Nothing hidden, but nothing automatic, and both developers have to remember to run it.
- **C. Hibernate `ddl-auto` generating the schema from entities.** The least SQL to write, but not versioned and unsafe for changes. It would compete with `schema.sql` as the source of truth.
- **D. Flyway or Liquibase.** Proper versioned migrations, but we chose not to add a migration tool for this project.

### 2. Hibernate schema behaviour

- **A. `validate` (chosen).** Hibernate checks entities against the real tables at startup and fails with a clear error on a mismatch. It never alters anything.
- **B. `none`.** No checking. A mismatch only shows up later as a runtime SQL error.
- **C. `update` / `create` / `create-drop`.** Hibernate changes the schema itself, which undermines `schema.sql` as the single source of truth.

### 3. Database creation

- **A. `createDatabaseIfNotExist=true` on the JDBC URL (chosen).** The driver creates the database on first connect, so `schema.sql` contains only tables and seeds. The MySQL user needs permission to create databases.
- **B. A separate one-time `setup.sql`** that creates both databases and grants privileges. Keeps privileges explicit, but adds a manual setup step.

### 4. Lookup data

- **A. Lookup tables (chosen)** with a foreign key from the referencing table, mapped as JPA entities.
- **B. Java enums** stored as strings. No table, but string-stored enums sort alphabetically and cannot carry a display label or retire values without a code change.
- **C. MySQL `ENUM` columns.** The database enforces values, but changes need an `ALTER TABLE` and mapping is awkward.

### 5. Retiring lookup values

- **A. An `active` flag (chosen).** Dropdowns show only active rows, and existing issues keep their value.
- **B. No flag.** Lookup values are permanent, and any removal is a hand-written change.

### 6. Seeding lookup rows

- **A. `INSERT ... ON DUPLICATE KEY UPDATE`, keyed on `code` (chosen).** Every restart re-asserts the file's values, so `schema.sql` is the single source of truth for reference data.
- **B. `INSERT IGNORE`.** Only adds missing rows, so edits to labels or ordering never reach existing databases, and it silently swallows unrelated errors.

### 7. Release 2 tables

- **A. Create the full schema from day one (chosen).** `users`, `roles` and `project_members` exist from the start, so Release 2 needs no schema edit for them.
- **B. Add them when Release 2 starts.** Cheap, because new tables are picked up automatically, but the schema would no longer be complete from the beginning.

### 8. Existing local databases

- **A. Drop and recreate once (chosen).** Clean, and cheap now because no real data exists yet.
- **B. Hand-write an `ALTER` for the lookup tables.** Keeps existing data, but is fiddly for a one-time change.

### 9. Test database

- **A. A separate `sourbeans_test` database on each local MySQL (chosen).** Real MySQL behaviour, dev data untouched, built from the same `schema.sql`.
- **B. H2 in memory.** Zero setup, but our SQL uses MySQL-specific syntax and dialect differences could hide bugs.
- **C. Testcontainers.** The most faithful, but adds Docker as a requirement, which ADR-0001 avoided.

### 10. Test data cleanup

- **A. `@Transactional` by default, explicit cleanup only where rollback is impossible (chosen).**
- **B. `@Transactional` everywhere.** Simplest, but tests that commit their own transactions leave rows behind.
- **C. Explicit cleanup in every test.** Works in any wiring, but is more code and easy to forget a table.

### 11. Credentials

- **A. A git-ignored `local.properties` imported automatically (chosen),** with a committed `local.properties.example` and environment variables as the deployment override.
- **B. Environment variables only.** Matches deployment, but a missing variable gives a confusing connection error.
- **C. A git-ignored profile file** that must be activated every time.
- **D. A config file outside the repo,** which cannot be committed by accident but is a less obvious setup step.
- **E. A shared username and password committed to the repo.** Rejected: it contradicts the charter's rule against committing passwords.

### 12. Tracking manual schema changes

- **A. The PR description and a change log in the repo (chosen).**
- **B. The PR description only.** The `ALTER` is buried in PR history.
- **C. A change log only.** A good record, but the reviewer has nothing to run during review.

## Decisions

1. **No migration tool.** The schema lives in a hand-written `src/main/resources/schema.sql`, run by Spring at startup with `spring.sql.init.mode=always`. Every table uses `CREATE TABLE IF NOT EXISTS`, so re-running it never drops data.
2. **Hibernate runs with `spring.jpa.hibernate.ddl-auto=validate`.** `update`, `create` and `create-drop` are not used in any profile.
3. **The full schema is created from the first version,** including `users`, `roles` and `project_members`, even though Release 2 is the first to use them.
4. **Database creation uses `createDatabaseIfNotExist=true`** on the JDBC URL. `CREATE DATABASE` and `USE` do not appear in `schema.sql`, because `USE` would send the test run's tables into the wrong database. The README states that the MySQL user needs permission to create databases.
5. **Status, priority and role values are lookup tables.** Statuses and priorities have a numeric primary key, a unique `code`, a display `label`, a `sort_order` and an `active` flag. Roles have a numeric primary key and a unique `name`, since they are never shown in a dropdown or retired.
6. **Application code refers to lookup values by `code` (or `name` for roles), never by numeric id.**
7. **Lookup rows are never deleted.** `ON DELETE RESTRICT` enforces this for rows in use, and unwanted values are retired by setting `active` to false.
8. **Lookup rows are seeded in `schema.sql` using `INSERT ... ON DUPLICATE KEY UPDATE`, keyed on `code`.** The file is the single source of truth for reference data, so manual edits to those rows in a database are overwritten at the next start.
9. **Initial lookup values:** statuses `TODO`, `IN_PROGRESS`, `DONE`; priorities `UNDEFINED`, `LOW`, `MEDIUM`, `HIGH`; roles `OWNER`, `MEMBER`.
10. **Schema changes to existing tables** are made by editing `schema.sql` so fresh databases are correct, plus a hand-run `ALTER` on each existing local database. The `ALTER` is recorded in both the PR description and a dated entry in `docs/schema-changes.md`. A schema PR does not merge until the reviewer has run it against both a clean database and an existing one.
11. **Existing local databases are rebuilt once.** Both developers drop and recreate `sourbeans` and `sourbeans_test`, then let the app build them from the final `schema.sql`.
12. **Tests use a separate `sourbeans_test` database** on each developer's local MySQL, built from the same `schema.sql`. H2 is not used.
13. **Tests are `@Transactional` by default.** A test that cannot use rollback cleans up after itself, deleting in foreign-key order. Cleanup never deletes from `statuses`, `priorities` or `roles`.
14. **Credentials live in a git-ignored `local.properties`,** imported with `spring.config.import=optional:file:./local.properties`. A committed `local.properties.example` shows the shape. Environment variables override the file and are the mechanism for the Ubuntu deployment. The test profile overrides only the datasource URL.
15. **Revisit trigger:** if the schema needs more than about two changes to existing tables, this ADR is reopened and a migration tool is reconsidered.

## Why

- A re-runnable `schema.sql` gives us what we wanted: the app applies the schema, and our data survives restarts. It also keeps the schema as plain SQL we can read and review.
- Skipping a migration tool is reasonable because the schema is modeled up front and we expect very few changes. Creating the whole schema on day one means that most of the schema is never touched again.
- `validate` is our safety net for the manual `ALTER` step. If one developer forgets to apply a change, the app tells them at startup instead of failing mysteriously on a page nobody tested.
- Lookup tables give us foreign-key integrity, correct sort order, friendly labels for dropdowns and a safe way to add or retire values, all without a code release. Status is the set we expect to grow, and using tables for all three keeps filters and dropdowns consistent.
- Treating `schema.sql` as the source of truth for lookup data keeps both machines identical, and it avoids the silent drift that `INSERT IGNORE` allows.
- A separate test database on real MySQL avoids dialect surprises and never touches development data, and `@Transactional` keeps tests independent with very little code.
- A git-ignored properties file that Spring imports automatically needs no profile to activate, works the same in IntelliJ and on the command line, and matches how secrets will be provided on the VM.

## Consequences / trade-offs

- Changes to existing tables are manual. They depend on both developers applying the `ALTER`, and on `validate` and the PR rule catching mistakes.
- `IF NOT EXISTS` only protects table creation. It will never modify a table that already exists.
- `INSERT ... ON DUPLICATE KEY UPDATE` overwrites manual edits to lookup rows at every start. That is intended, but it can surprise someone who edits a row in IntelliJ's database tool.
- InnoDB consumes auto-increment values on duplicate-key inserts, so lookup ids may have gaps after restarts. Nothing may depend on specific id numbers.
- The MySQL user needs permission to create databases, which is broader than the app needs once running. Deployment should use a more restricted user.
- The Release 2 tables sit unused in Release 1. If one turns out to be wrong, fixing it uses one of the expected `ALTER`s.
- Hibernate's `validate` can complain about harmless type differences (for example `TIMESTAMP` or `TEXT` columns), so a few `columnDefinition` tweaks may be needed.
- Lookup tables add entity classes and joins compared to enums. Role checks in security code will go through the `roles` table, so code must always refer to `OWNER` and `MEMBER` by name.
- Tests that opt out of rollback are responsible for their own cleanup, and a forgotten cleanup shows up as leftover rows in `sourbeans_test`.
- IntelliJ's Database tool remains useful for browsing and querying, but it is not a source of truth for the schema.

## Kickoff choices

- [ ] Schema management: hand-written `schema.sql` run at startup, no Flyway or Liquibase
- [ ] Hibernate `ddl-auto=validate`
- [ ] Database created via `createDatabaseIfNotExist=true`
- [ ] Lookup tables for statuses, priorities and roles, with `code`, `label`, `sort_order` and `active` on statuses and priorities
- [ ] Lookup seeds use `ON DUPLICATE KEY UPDATE`
- [ ] Full schema created from day one, including Release 2 tables
- [ ] Existing local databases dropped and recreated once
- [ ] Separate `sourbeans_test` database, `@Transactional` by default
- [ ] Credentials in a git-ignored `local.properties`, with a committed `.example`
- [ ] Schema changes tracked in the PR description and `docs/schema-changes.md`
- [ ] Review the final `schema.sql` against these decisions before merging

## Approval

- Rory: _________ / Date: _________
- Josh: _________ / Date: _________
