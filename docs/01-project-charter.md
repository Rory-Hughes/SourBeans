# 01 — Project Charter

**Project:** SourBeans
**Status:** Planning — MVP scope accepted in [ADR-0001](decisions/0001-initial-scope.md); charter review tracked separately
**Team:** Rory Hughes and Joshua Lynch
**Purpose:** Independent practice for PROG1346A concepts

## Problem statement

Small student projects need a simple way to keep track of what needs doing, what is in progress, and what is finished. Existing platforms are powerful, but implementing a narrow tracker ourselves creates hands-on experience with a typical data-driven web application's layers and workflows.

## Product vision

A user can open a project, add an issue, update its status and priority, and see the current work queue. The same core actions are available in a browser (Thymeleaf) and programmatically (REST API).

## Target user

Initially, one or two student developers managing their own practice projects. In the secured phase, registered users with project-specific permissions.

## Learning outcomes

1. Build an MVC-style Spring Boot web application with server-rendered forms.
2. Represent related domain entities using JPA (`Project` 1→many `Issue`).
3. Design and implement a small JSON REST API with validation and consistent error responses.
4. Add authentication, authorization and JWT for REST API endpoints.
5. Run the application and database on an Ubuntu VM.
6. Collaborate using small GitHub issues and reviewed pull requests.

## Scope

### Release 1: Assignment 2-aligned practice

- Project CRUD.
- Issue CRUD within a project.
- Issue status and priority.
- Project detail page with issue list and filters.
- REST API for projects and issues under `/api`, with RFC 9457 Problem Details error responses.
- Data persistence via JPA and MySQL.
- Web and API input validation, not-found handling, and basic tests.

### Release 2: Assignment 3-aligned practice

- Account registration/login (or instructor-approved equivalent).
- Securely hashed passwords.
- Project membership with `OWNER` and `MEMBER` permissions.
- JWT authentication for API requests; session/form authentication for server-rendered pages.
- Authorization checks on every project/issue read and modification.
- Ubuntu VM deployment with secrets managed outside source control.
- Security-focused tests.

### Explicitly deferred

Comments, issue assignees, attachments, notifications, email, activity timeline, GitHub synchronization, advanced search, real-time updates, mobile app, and multi-tenant enterprise features.

## Definition of success

- A new developer can follow the README and run the app locally.
- The team demonstrates project + issue CRUD through both Thymeleaf and JSON API.
- Project-to-issue persistence works across application restarts.
- Invalid input gives clear feedback, not a server error.
- Both developers contribute code, review each other's PRs, and understand the main layers.
- Before any network-accessible deployment, authentication and authorization tests pass.
- Final secured application runs on an Ubuntu VM and can be demonstrated.

## Constraints and assumptions

- Two part-time student developers with classwork as the higher priority.
- Prefer concepts covered in class; additional dependencies require team discussion.
- Keep each feature small enough for one PR and review.
- Start without login _only on localhost_; secure before network exposure.
- Dates are suggested milestones, not academic due dates.
- Exact requirements of the graded assignments may differ from the course outline.

## Project risks

| Risk                        | Impact                                | Mitigation                                                                         |
| --------------------------- | ------------------------------------- | ---------------------------------------------------------------------------------- |
| Too many features           | Practice project becomes unmanageable | Freeze MVP and track ideas as stretch issues                                       |
| Merge conflicts             | Lost time                             | Small PRs, separate files where practical, integrate frequently                    |
| Security bolted on too late | Architecture rework                   | Use service layer, DTOs, and project-scoped access from the beginning              |
| Inconsistent API/web logic  | Bugs                                  | Share domain services; do not place business rules in controllers                  |
| Database setup friction     | Slow onboarding                       | Use the [local development guide](06-local-development.md); never commit passwords |

## Team sign-off

- [ ] Rory has reviewed this charter.
- [x] Josh has reviewed this charter.
- [x] Scope and terminology are agreed in ADR-0001.
- [ ] Open decisions are resolved or explicitly deferred.

> ADR-0001 contains both developers' approvals of scope and architecture. The individual charter-review checkboxes above are tracked separately; approval of the ADR is not recorded as a charter review.
