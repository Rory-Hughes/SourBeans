# 02 — Requirements and User Stories

**Status:** Draft. Requirement IDs are stable identifiers for future GitHub Issues and tests.

## Domain vocabulary

- **Project:** Container for issues, with a name and description.
- **Issue:** Trackable piece of work belonging to exactly one project.
- **Status:** `OPEN`, `IN_PROGRESS`, or `CLOSED`.
- **Priority:** `LOW`, `MEDIUM`, or `HIGH`.
- **Member / Owner:** Release 2 authorization roles _within a project_ (not MVP features).

## Release 1 — Functional requirements

| ID   | User story                                       | Acceptance criteria                                                                                                                         |
| ---- | ------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------- |
| P-01 | As a user, I can create a project.               | Name is required (3–80 characters); description is optional (up to 1,000 characters); valid submission persists and appears in list.        |
| P-02 | I can view all projects and open one.            | Project list displays name; detail displays description and associated issues. Unknown ID returns 404.                                      |
| P-03 | I can update project details.                    | Valid changes persist; invalid name displays web validation or HTTP 400 for API.                                                            |
| P-04 | I can delete an empty project.                   | Delete succeeds if it has no issues; deletion of a nonempty project is rejected with a helpful message / HTTP 409.                          |
| I-01 | I can create an issue inside a project.          | Title required (5–120 chars), description optional (up to 5,000); default status `OPEN`, priority `MEDIUM`; issue is linked to its project. |
| I-02 | I can see and open issues for a project.         | Issue list shows title, status and priority; issue detail shows full fields; unrelated-project issue data is not included.                  |
| I-03 | I can edit an issue and change status/priority.  | Changes persist; only valid enum values accepted; updated timestamp changes.                                                                |
| I-04 | I can delete an issue.                           | Issue is removed; project remains; subsequent issue GET returns 404.                                                                        |
| I-05 | I can filter issues.                             | Project issue list supports optional status and/or priority filter; no filters shows all.                                                   |
| A-01 | As a client, I can manage projects through REST. | JSON CRUD endpoints under `/api` are available with expected 200/201/204/400/404/409 outcomes.                                              |
| A-02 | As a client, I can manage issues through REST.   | JSON endpoints support listing/creating issues by project and getting/updating/deleting an issue.                                           |
| V-01 | As a user, I see useful validation errors.       | Empty/oversized fields produce clear messages; API errors use RFC 9457 Problem Details; bad IDs do not generate 500s.                       |

> **Not for public release:** Release 1 REST endpoints do not require authentication. Run this build only in a controlled local environment.

## Release 2 — Security and deployment requirements

| ID   | User story                                 | Acceptance criteria                                                                                                             |
| ---- | ------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------- |
| S-01 | I can sign up and sign in.                 | Passwords stored as one-way hashes; invalid login gives a generic failure.                                                      |
| S-02 | I can authenticate to the API.             | Valid login returns a time-limited JWT; invalid/expired/missing token is rejected for protected routes.                         |
| S-03 | I can access only my projects.             | Users cannot read or change projects or issues without membership, including by guessing IDs.                                   |
| S-04 | As project owner, I can manage membership. | Owners may add/remove members; members cannot grant themselves owner privileges.                                                |
| S-05 | I can use Thymeleaf pages after login.     | Web form login uses secure server-side sessions; session-authenticated forms are CSRF-protected.                                |
| D-01 | We can deploy to Ubuntu.                   | App and database configuration are externalized; service runs on VM; restart retains data; manual deployment instructions work. |
| T-01 | We can prove access control works.         | Tests cover valid authentication, 401/403 (or concealed 404), and cross-project access denial.                                  |

## Main journeys

### Create and complete an issue (Release 1)

1. Open a project.
2. Choose **New issue**.
3. Submit title, description, and optional priority.
4. Find the new issue in the project list with status `OPEN`.
5. Edit it to `IN_PROGRESS`, then `CLOSED`.
6. Confirm the updated status is visible through both web page and API.

### Protected project access (Release 2)

1. Sign in as a project member.
2. Open an authorized project's issue list.
3. Try a different project's URL or API ID.
4. Access is denied without exposing issue contents.

## Nonfunctional requirements

- **Correctness:** Server-side validation; enforce database referential integrity.
- **Maintainability:** Web and API controllers use shared services, not duplicated persistence/business logic.
- **Testability:** Unit tests for services plus controller/API tests for key workflows.
- **Usability:** Clear navigation and messages on success, missing content, and invalid input.
- **Security:** No secrets in Git; use password hashing and least-privilege authorization in Release 2.
- **Operations:** Document local setup and Ubuntu deployment.

## Deferred ideas (not acceptance criteria)

Comments, assignees, labels, sorting/pagination, issue history, email notifications, a Kanban board, attachments, admin dashboard, OpenAPI docs, and GitHub integration.
