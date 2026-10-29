# 04 — Milestones and Starter Backlog

**Status:** Proposed work order. These are practice milestones, not official assignment deadlines.

## Milestone plan

| Milestone       | Focus                                                                      | Demo exit criterion                                             |
| --------------- | -------------------------------------------------------------------------- | --------------------------------------------------------------- |
| M0 — Kickoff    | Scope, GitHub repository, environment, app skeleton, MySQL connectivity    | Both developers run the same skeleton locally                   |
| M1 — Projects   | Project persistence, REST CRUD, Thymeleaf forms, validation                | Create/update/list/delete an empty project from UI and API      |
| M2 — Issues     | Issue persistence and project relationship, CRUD, status/priority, filters | Create and close an issue from UI; inspect same result via API  |
| M3 — Security   | Users, login, membership rules, Spring Security, JWT, tests                | Authorized user succeeds; unknown/cross-project user is blocked |
| M4 — Deployment | Ubuntu VM service, database config, tests, README                          | Live secured demo with persistence after restart                |

**Suggested course alignment:** M0–M2 for Assignment 2 concepts, M3–M4 for Assignment 3 concepts. No exact schedule assumed.

## Starter GitHub Issues

Copy each row into a new GitHub Issue, adding a description and acceptance checklist. `Rory` and `Josh` are suggested initial owners, not fixed assignments.

| ID       | Suggested issue title                                           | Milestone | Lead     | Depends on         |
| -------- | --------------------------------------------------------------- | --------- | -------- | ------------------ |
| SET-01   | Initialize Spring Boot + Maven repository                       | M0        | A        | —                  |
| SET-02   | Configure local MySQL and Spring Data JPA                       | M0        | B        | SET-01             |
| SET-03   | Establish package layout, DTO convention, and error responses   | M0        | Together | SET-01             |
| PROJ-01  | Implement Project entity, repository and service                | M1        | A        | SET-02             |
| PROJ-02  | Add Project REST endpoints and validation tests                 | M1        | A        | PROJ-01, SET-03    |
| PROJ-03  | Add Thymeleaf project list/detail/create/edit forms             | M1        | B        | PROJ-01            |
| ISSUE-01 | Implement Issue entity and Project→Issue relationship           | M2        | B        | PROJ-01            |
| ISSUE-02 | Add Issue REST endpoints and validation tests                   | M2        | B        | ISSUE-01, SET-03   |
| ISSUE-03 | Add Thymeleaf issue list/create/detail/edit forms               | M2        | A        | ISSUE-01           |
| ISSUE-04 | Implement status/priority filters and empty-project delete rule | M2        | Together | ISSUE-02, ISSUE-03 |
| QA-01    | Verify end-to-end CRUD and document manual test steps           | M2        | Together | M1, M2             |
| AUTH-01  | Add User account and secure password storage                    | M3        | A        | QA-01              |
| AUTH-02  | Add Spring Security form login and protected pages              | M3        | A        | AUTH-01            |
| AUTH-03  | Add JWT issuance and bearer validation for API                  | M3        | B        | AUTH-01            |
| AUTH-04  | Implement project membership authorization and negative tests   | M3        | B        | AUTH-02, AUTH-03   |
| DEP-01   | Document and deploy secured app to Ubuntu VM                    | M4        | Together | AUTH-04            |
| DEP-02   | Test restart, protected endpoints, and final walkthrough        | M4        | Together | DEP-01             |

## Suggested first week / first few sessions

**Session 1 (together):** Review charter; decide name, DB setup, package name and MVP boundaries; create repository and issues.

**Session 2:** One developer initializes Spring Boot; the other documents local DB setup and the starter configuration. Pair to verify both machines can run it.

**Session 3:** Rory starts project service/persistence; Josh sketches Thymeleaf screens and tests acceptance criteria. Agree interfaces before parallel implementation.

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
