# SourBeans

> A small, GitHub-inspired issue tracker built by two student developers to practise enterprise Java development. This is independent practice, not a course assignment submission.

**Status:** Planning — initial scope approved; no application code yet  
**Developers:** Rory Hughes and Joshua Lynch  
**Repository owner:** Rory Hughes  
**Architecture decision:** [ADR-0001 — Initial Scope and Architectural Starting Point](docs/decisions/0001-initial-scope.md) (approved by both developers, 2026-10-29)

## The idea

Build a web application for organizing work into **projects** and **issues**. Each issue has a title, description, status, and priority. The same core operations will be available through a **Thymeleaf web interface** and a **JSON REST API**.

The first release deliberately focuses on project and issue CRUD. Accounts, authorization, JWT, and Ubuntu deployment follow after the first release passes its tests.

## Learning goals

- Use Spring Boot, Spring MVC, Thymeleaf, Bean Validation, REST controllers, and shared services.
- Model and persist a one-to-many `Project` → `Issue` relationship using Spring Data JPA and MySQL.
- Add Spring Security, password hashing, session-based browser login, project-level authorization, and JWT-protected API endpoints in the next phase.
- Deploy the **secured** application to an Ubuntu VM.
- Practise GitHub Issues, Kanban task ownership, reviewed pull requests, testing, and technical documentation.

These goals align with topics from PROG1346A – Enterprise Java Assignments 2 and 3, but this project is separate from graded assignments.

## Approved starting-point decisions

| Area | Decision |
| --- | --- |
| Name | **SourBeans** |
| Java base package | `ca.humanbeans.sourbeans` |
| Application | Single Spring Boot application (not microservices) |
| Browser + API | Thymeleaf HTML controllers and JSON REST controllers sharing services |
| Database | **MySQL installed locally** on each developer's machine (no container for initial development) |
| Persistence | Spring Data JPA, initially one `Project` with many `Issue` records |
| API base path | `/api` |
| API errors | [RFC 9457 Problem Details](https://www.rfc-editor.org/info/rfc9457/), using `application/problem+json` |
| Team workflow | GitHub Issues, Kanban assignments, and reviewed pull requests |
| Deployment | Only after authentication and authorization have been implemented and tested |

See [ADR-0001](docs/decisions/0001-initial-scope.md) for the full agreement and both developers' approvals.

## MVP (Release 1)

- Create, list, view, edit, and delete projects.
- Create, list, view, edit, and delete issues within a project.
- Track issue status (`OPEN`, `IN_PROGRESS`, `CLOSED`) and priority (`LOW`, `MEDIUM`, `HIGH`).
- Filter a project's issues by status and priority.
- Persist data in MySQL through JPA.
- Offer both Thymeleaf pages and `/api` JSON endpoints, using shared business services.
- Validate inputs and return useful browser errors or RFC 9457 API errors.

**Deferred:** User accounts/security (Release 2), comments, assignees, attachments, notifications, advanced search, and other stretch features.

**Important:** Release 1 intentionally has no authentication. Keep it **local-only**; do not expose it to a network or deploy it publicly. Deployment comes after security tests pass.

## Technology stack

**Initial:** Java (compatible JDK version to be agreed), Spring Boot, Spring MVC, Thymeleaf, Bean Validation, Spring Data JPA, local MySQL, Maven, Git/GitHub.

**Later:** Spring Security, browser sessions, JWT-protected REST API, Ubuntu VM deployment.

Exact version numbers and schema-migration tooling have **not** yet been approved.

## Getting started

The Spring Boot project skeleton and build scripts have not been created yet. The following is **environment preparation**, not a working application launch procedure:

1. Both developers install a compatible JDK and **MySQL locally**. Choose matching major versions together in the setup issue; Docker is not part of the approved starting point.
2. Follow [06 — Local Development Setup](docs/06-local-development.md) to create each developer's own `sourbeans` database, local MySQL account, and untracked configuration.
3. Use the agreed Java package `ca.humanbeans.sourbeans` when generating the Spring Boot project.
4. Configure Spring's datasource via local environment variables. Never commit passwords, local profiles containing secrets, or database dumps.
5. Use the GitHub Kanban board to select/assign setup issues and open reviewed pull requests. Once the application skeleton exists, this README will gain verified build, test, and run commands.

## Documentation

- [Project charter](docs/01-project-charter.md) — project purpose, release boundaries, success criteria.
- [Requirements and user stories](docs/02-requirements.md) — functional behavior and acceptance criteria.
- [Architecture, data model, and API](docs/03-architecture-and-api.md) — layers, entities, endpoint sketch, Problem Details responses.
- [Milestones and starter backlog](docs/04-milestones-and-backlog.md) — suggested issue sequence; live ownership is in Kanban.
- [Team workflow](docs/05-team-workflow.md) — PR reviews, branch habits, testing.
- [Local development setup](docs/06-local-development.md) — locally installed MySQL for each developer.
- [ADR-0001](docs/decisions/0001-initial-scope.md) — accepted decisions and developer approvals.

> **Course note:** Follow instructor restrictions on AI tools in graded work. Keep this independent learning project separate from submissions.
