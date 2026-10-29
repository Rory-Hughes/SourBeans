# 05 — Two-Person Team Workflow

**Goal:** Learn professional collaboration without adding unnecessary process.

## Repository habits

- `main` always builds and contains reviewed work.
- Work on one issue per branch (`feature/PROJ-01-project-entity`, `fix/ISSUE-04-filter-bug`).
- Make small commits with descriptive messages, e.g., `feat: add project entity and repository`.
- Open a pull request before merging to `main`.
- Each person reviews the other's PR for correctness, clarity, tests and scope.
- Rebase/merge latest `main` frequently to avoid long-running conflicts.

## Team ownership

Splitting by feature rather than one person always doing frontend and the other always doing backend encourages broader learning:

| Work | Primary lead | Reviewer / pairing partner |
|---|---|---|
| Projects domain + API | A | B |
| Issues domain + API | B | A |
| Projects Thymeleaf pages | B | A |
| Issues Thymeleaf pages | A | B |
| Form login | A | B |
| JWT API security | B | A |
| Authorization and Ubuntu deployment | Together | Together |

Switch roles if one person wants additional practice in a particular area. Review both directions.

## Integration checkpoints

Before opening a PR:
1. Pull latest `main` and resolve conflicts.
2. Run Maven build and the test suite (`./mvnw test` once Maven Wrapper exists; Windows: `mvnw.cmd test`).
3. Exercise the relevant Thymeleaf or API path manually.
4. Include request/response examples or screenshots if they clarify behavior.

At least once per work session, spend 10–15 minutes together reviewing changes, blockers, and next issues.

## Lightweight documentation rules

- **Charter:** Update when goals or boundaries change.
- **Requirements:** Change acceptance criteria *before* implementation when possible.
- **Architecture/API:** Update when adding entities, endpoints, or auth rules.
- **Decision record:** Record choices that would be expensive to reverse (not every minor preference).
- **README:** Keep setup instructions accurate from the moment code exists.

## PR checklist

Use the template in `.github/PULL_REQUEST_TEMPLATE.md`. Each review should answer: does the change satisfy its issue, are edge cases tested, can both teammates explain it, and does it remain within scope?

## Academic integrity

This repository is intended as independent learning practice. The PROG1346A course outline restricts AI-generator use for graded submissions. Follow instructor policies and avoid presenting AI-generated practice materials as independently authored graded assignments.
