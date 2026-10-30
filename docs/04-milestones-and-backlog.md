# 04 — Milestones and Starter Backlog

**Status:** Proposed work order aligned with [accepted ADR-0001](decisions/0001-initial-scope.md). These are practice milestones, not official assignment deadlines; GitHub Kanban controls live assignments.

## Milestone plan

| Milestone       | Focus                                                                      | Demo exit criterion                                             |
| --------------- | -------------------------------------------------------------------------- | --------------------------------------------------------------- |
| M0 — Kickoff    | Approved scope, GitHub repository, local MySQL setup, app skeleton         | Both developers run the same skeleton with their own local MySQL |
| M1 — Projects   | Project persistence, REST CRUD, Thymeleaf forms, validation                | Create/update/list/delete an empty project from UI and API      |
| M2 — Issues     | Issue persistence and project relationship, CRUD, status/priority, filters | Create and close an issue from UI; inspect same result via API  |
| M3 — Security   | Users, login, membership rules, Spring Security, JWT, tests                | Authorized user succeeds; unknown/cross-project user is blocked |
| M4 — Deployment | Ubuntu VM service, database config, tests, README                          | Live secured demo with persistence after restart                |

**Suggested course alignment:** M0–M2 for Assignment 2 concepts, M3–M4 for Assignment 3 concepts. No exact schedule assumed.

## Starter GitHub Issues

Use these as candidate issues and sequencing guidance. Create/update them on the GitHub Kanban board, which is the **source of truth** for assignment, status and priorities; do not treat this document as a live assignment list.

| ID | Candidate GitHub issue | Milestone | Depends on |
| --- | --- | --- | --- |
| SET-01 | Initialize Spring Boot + Maven repository | M0 | — |
| SET-02 | Configure local MySQL and Spring Data JPA | M0 | SET-01 |
| SET-03 | Establish package layout, DTO convention, and error responses | M0 | SET-01 |
| PROJ-01 | Implement Project entity, repository and service | M1 | SET-02 |
| PROJ-02 | Add Project REST endpoints and validation tests | M1 | PROJ-01, SET-03 |
| PROJ-03 | Add Thymeleaf project list/detail/create/edit forms | M1 | PROJ-01 |
| ISSUE-01 | Implement Issue entity and Project→Issue relationship | M2 | PROJ-01 |
| ISSUE-02 | Add Issue REST endpoints and validation tests | M2 | ISSUE-01, SET-03 |
| ISSUE-03 | Add Thymeleaf issue list/create/detail/edit forms | M2 | ISSUE-01 |
| ISSUE-04 | Implement status/priority filters and empty-project delete rule | M2 | ISSUE-02, ISSUE-03 |
| QA-01 | Verify end-to-end CRUD and document manual test steps | M2 | M1, M2 |
| AUTH-01 | Add User account and secure password storage | M3 | QA-01 |
| AUTH-02 | Add Spring Security form login and protected pages | M3 | AUTH-01 |
| AUTH-03 | Add JWT issuance and bearer validation for API | M3 | AUTH-01 |
| AUTH-04 | Implement project membership authorization and negative tests | M3 | AUTH-02, AUTH-03 |
| DEP-01 | Document and deploy secured app to Ubuntu VM | M4 | AUTH-04 |
| DEP-02 | Test restart, protected endpoints, and final walkthrough | M4 | DEP-01 |

## Suggested first week / first few sessions

**Session 1 (together):** Review accepted ADR-0001 and charter, synchronize GitHub Kanban issues, and confirm both developers understand the agreed MVP. The project name, package, local MySQL, API prefix, and error format are already decided.

**Session 2:** Follow the [local MySQL setup guide](06-local-development.md) independently; implement the Spring Boot skeleton and externalized datasource configuration. Pair to verify both machines can run it. Choose compatible version numbers together.

**Session 3:** Take the next assignments from GitHub Kanban; agree service interfaces and acceptance tests before parallel implementation.

**Session 4:** Swap/review PRs and do a shared integration demo. Fix any design disagreements in docs before proceeding to Issue CRUD.

## Definition of Done — every implementation issue

- [ ] Required behavior works against local database.
- [ ] Relevant validation / error path is handled.
- [ ] Test(s) added or rationale documented if not feasible.
- [ ] No credentials or secrets are committed.
- [ ] PR explains change and includes manual verification steps.
- [ ] Other developer reviews; no self-merge without review.
- [ ] The app still builds and existing tests pass after merge.
- [ ] Documentation is updated if routes, data model or behavior changed.

## Scope control

New ideas should be logged as **stretch** issues. Do not pull them into M0–M2 unless both developers agree a feature is required for the promised MVP.
