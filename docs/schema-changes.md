# Schema changes

## 2026-10-31 — SET-02

Moved the accepted Release 1 schema from `SQL/schema.sql` to `src/main/resources/schema.sql`. Table definitions are unchanged; no manual ALTER is required. Fresh databases get `projects` and `issues` at startup. Existing matching tables are preserved. Existing schemas that differ require deliberate review; startup scripts do not migrate them.

## 2026-11-01 — PROJ-01

DB must be Dropped and rerun.

- added proper constants from the db for name and description.
- updated_at was added to both of the current tables.
- DELETE will no longer cascade as due to 02-Requirements.md databases that have issues cannot be deleted.
