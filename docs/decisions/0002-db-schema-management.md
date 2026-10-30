# ADR-0002: Database Schema Management, Reference Values and Test Data

**Status:** Discussion / Pending Approval \
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

- **A. `createDatabaseIfNotExist=true` on the JDBC URL.** The driver creates the database on first connect, so there is no setup step. The app's MySQL user needs permission to create databases, and the new database gets whatever default charset and collation each machine's server has.
- **B. A separate one-time `setup.sql` (chosen)** that creates both databases with an explicit charset and collation and grants the app user its privileges. Both databases are identical on both machines and privileges stay explicit, but it adds a manual setup step on each machine.

### 4. Reference values (status, priority, role)

- **A. Lookup tables** with a foreign key from the referencing table, mapped as JPA entities. Gives foreign-key integrity, labels and ordering in the database, and new values by `INSERT`, but adds entity classes, joins and seed-data handling.
- **B. Java enums stored as strings (chosen).** Mapped with `@Enumerated(EnumType.STRING)` into `VARCHAR` columns. No extra tables, entities or joins, and values are checked at compile time. The display label and sort order live on the enum in Java, because a string column sorts alphabetically and cannot carry them. Adding or retiring a value needs a code change.
- **C. MySQL `ENUM` columns.** The database enforces values, but changes need an `ALTER TABLE` and mapping is awkward.

### 5. Retiring lookup values

Not applicable. Lookup tables were not chosen in option 4, so there is no `active` flag. Retiring an enum value is covered in Decision 7.

### 6. Seeding lookup rows

Not applicable. Reference values are enum constants, so there are no rows to seed and `schema.sql` contains no reference data.

### 7. Release 2 tables

- **A. Create the full schema from day one.** `users` and `project_members` exist from the start, so Release 2 needs no schema edit for them, but they sit unused during Release 1.
- **B. Add them when Release 2 starts (chosen).** Cheap, because `schema.sql` is re-runnable and a new `CREATE TABLE IF NOT EXISTS` is picked up automatically at the next start with no `ALTER`. The Release 1 schema holds only what Release 1 uses, but the schema is no longer complete from the beginning.

### 8. Existing local databases

- **A. Drop and recreate once (chosen).** Clean, and cheap now because no real data exists yet.
- **B. Hand-write an `ALTER` to bring existing tables in line with the final schema.** Keeps existing data, but is fiddly for a one-time change.

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
3. **Release 1 creates only the tables Release 1 uses.** The Release 2 tables (`users`, `project_members`) are added to `schema.sql` when Release 2 starts. As new tables they are created by the re-runnable script and need no `ALTER`.
4. **Databases are created by a committed, one-time `setup.sql`,** run on each machine by a MySQL user with admin rights. It creates `sourbeans` and `sourbeans_test` with `CHARACTER SET utf8mb4` and `COLLATE utf8mb4_unicode_ci`, and grants the app user privileges on those two databases only, with no permission to create databases. `createDatabaseIfNotExist` is not used on the JDBC URLs. `CREATE DATABASE` and `USE` do not appear in `schema.sql`, because `USE` would send the test run's tables into the wrong database. `setup.sql` contains only a placeholder password for the app user, never a real one, and the README documents running it as the first setup step.
5. **Status, priority and role values are Java enums stored as strings.** They are mapped with `@Enumerated(EnumType.STRING)` into `VARCHAR` columns. Each enum carries its own display label and sort order. There are no lookup tables.
6. **The stored string is the enum constant's name.** Constants are never renamed casually, because a rename is a data change and needs an `UPDATE` on every existing database. Code and queries must never depend on the alphabetical order of the stored strings.
7. **Enum constants are never deleted while rows use them.** A value that should no longer be offered stays in the enum, is flagged as retired in Java, and is left out of dropdowns, so existing issues still load. Adding a value means adding a constant and releasing the code.
8. **`schema.sql` contains no reference data.** Reference values exist only as enum constants.
9. **Initial enum values:** statuses `TODO`, `IN_PROGRESS`, `DONE`; priorities `UNDEFINED`, `LOW`, `MEDIUM`, `HIGH`; roles `OWNER`, `MEMBER`. Roles are first used when Release 2 adds `project_members`.
10. **Schema changes to existing tables** are made by editing `schema.sql` so fresh databases are correct, plus a hand-run `ALTER` on each existing local database. The `ALTER` is recorded in both the PR description and a dated entry in `docs/schema-changes.md`. A schema PR does not merge until the reviewer has run it against both a clean database and an existing one. Adding a new table or a new enum constant does not count as a change to an existing table.
11. **Existing local databases are rebuilt once.** Both developers drop `sourbeans` and `sourbeans_test`, run `setup.sql`, then let the app build the tables from the final `schema.sql`. This also replaces any database that was created earlier with a machine's default charset.
12. **Tests use a separate `sourbeans_test` database** on each developer's local MySQL, built from the same `schema.sql`. H2 is not used.
13. **Tests are `@Transactional` by default.** A test that cannot use rollback cleans up after itself, deleting in foreign-key order.
14. **Credentials live in a git-ignored `local.properties`,** imported with `spring.config.import=optional:file:./local.properties`. A committed `local.properties.example` shows the shape. Environment variables override the file and are the mechanism for the Ubuntu deployment. The test profile overrides only the datasource URL. The password a developer sets for the app user when running `setup.sql` is the one they put in `local.properties`.
15. **Revisit trigger:** if the schema needs more than about two changes to existing tables, this ADR is reopened and a migration tool is reconsidered.

## Why

- A re-runnable `schema.sql` gives us what we wanted: the app applies the schema, and our data survives restarts. It also keeps the schema as plain SQL we can read and review.
- Skipping a migration tool is reasonable because the schema is modeled up front and we expect very few changes. New tables are created automatically by the re-runnable script, so only changes to existing tables are manual.
- Adding the Release 2 tables when Release 2 starts keeps the Release 1 schema small and avoids carrying unused tables, at no extra cost because new tables need no `ALTER`.
- `validate` is our safety net for the manual `ALTER` step. If one developer forgets to apply a change, the app tells them at startup instead of failing mysteriously on a page nobody tested.
- A one-time `setup.sql` makes both machines create their databases with the same charset and collation, instead of depending on whatever defaults each server happens to have. It also lets the app user run with only the privileges it needs, and it separates database creation from table creation: a script creates the databases, and the app builds the tables.
- Java enums fit this project because the value sets are small and change rarely. They are checked at compile time, work directly in switch statements and security checks, and need no extra entity classes, joins or seed data. Both machines stay identical because the values come from the code, not from rows in each database.
- A separate test database on real MySQL avoids dialect surprises and never touches development data, and `@Transactional` keeps tests independent with very little code.
- A git-ignored properties file that Spring imports automatically needs no profile to activate, works the same in IntelliJ and on the command line, and matches how secrets will be provided on the VM.

## Consequences / trade-offs

- Every machine needs a manual setup step before the first run. If someone skips `setup.sql`, the app fails to connect with an unknown-database error, so the README must list it first.
- Tables take their charset and collation from the database. A database created by hand instead of by `setup.sql` will silently differ, and `validate` does not check charset.
- The app user still needs table-level privileges (create, alter, index, references, and the usual data access) on both databases, because `schema.sql` runs at startup. It does not need permission to create databases.
- `setup.sql` creates a user, so its password must stay a placeholder. A real password belongs only in the developer's `local.properties`.
- Changes to existing tables are manual. They depend on both developers applying the `ALTER`, and on `validate` and the PR rule catching mistakes.
- `IF NOT EXISTS` only protects table creation. It will never modify a table that already exists.
- Adding or retiring a status, priority or role needs a code change and a release. It cannot be done with a database `INSERT`.
- The database does not enforce the allowed values, because the columns are plain `VARCHAR`. A bad string written by hand or by a bug only shows up when Hibernate fails to read the row.
- Renaming an enum constant, or deleting one that rows still use, breaks reading existing data until every database is updated.
- Ordering by a status or priority column in SQL sorts alphabetically, not by meaning. Sorting must be done in Java using the enum's sort order, or with an explicit `CASE` in the query.
- Retired values stay in the enum code indefinitely.
- If a Release 1 table later needs a reference to `users` (for example a creator or assignee column), adding it is an `ALTER` on an existing table and counts toward the revisit trigger. Release 1 design should decide early whether any such column is needed.
- Hibernate's `validate` can complain about harmless type differences (for example `TIMESTAMP` or `TEXT` columns), so a few `columnDefinition` tweaks may be needed.
- Tests that opt out of rollback are responsible for their own cleanup, and a forgotten cleanup shows up as leftover rows in `sourbeans_test`.
- IntelliJ's Database tool remains useful for browsing and querying, but it is not a source of truth for the schema.

## Kickoff choices

- [x] Schema management: hand-written `schema.sql` run at startup, no Flyway or Liquibase
- [x] Hibernate `ddl-auto=validate`
- [ ] Databases created by a one-time `setup.sql` with explicit `utf8mb4` charset and collation, no `createDatabaseIfNotExist`
- [ ] App user limited to the two databases, with no permission to create databases
- [x] Status, priority and role values as Java enums stored as strings, with label and sort order in the enum
- [x] Retired enum values stay in the code and are hidden from dropdowns
- [ ] Release 1 schema only; `users` and `project_members` added when Release 2 starts
- [x] Existing local databases dropped, then rebuilt with `setup.sql` and the app
- [x] Separate `sourbeans_test` database, `@Transactional` by default
- [x] Credentials in a git-ignored `local.properties`, with a committed `.example`
- [x] Schema changes tracked in the PR description and `docs/schema-changes.md`
- [ ] Review the final `schema.sql` and `setup.sql` against these decisions before merging

## Approval

- Rory: _________ / Date: _________
- Josh: Joshua Lynch / Date: 2026-10-30
