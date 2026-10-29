# SourBeans

> A small, GitHub-inspired issue tracker built by two student developers to practise enterprise Java development. This project is independent practice, not a course assignment submission.

**Status:** Planning — no application code yet  
**Team:** Rory and Josh  
**Project name:** SourBeans

## The idea

Build a web app where people organize work into **projects** and **issues**. Each issue has a title, description, status, and priority. Users can manage issues through a **Thymeleaf web interface** and a **JSON REST API**.

We will deliver an intentionally small, working application first, then add accounts, access control, JWT-secured API access, and deployment to an Ubuntu VM.

## Learning goals

- Apply Spring Boot, MVC/controllers, Thymeleaf, server-side form validation, REST APIs, and JPA relationships.
- Add Spring Security, password hashing, authentication, authorization, and JWT.
- Deploy a Java application backed by a database to an Ubuntu virtual machine.
- Practise Git collaboration, code review, testing, and technical documentation.

The goals align with the topics listed for Assignments 2 and 3 in **PROG1346A – Enterprise Java**; these documents are not the instructor's detailed assignment specifications.

## MVP (first release)

- Create, list, view, edit, and delete projects.
- Create, list, view, edit, and delete issues inside a project.
- Set issue status (`OPEN`, `IN_PROGRESS`, `CLOSED`) and priority (`LOW`, `MEDIUM`, `HIGH`).
- Filter a project's issues by status and priority.
- Persist data in MySQL using Spring Data JPA; one project has many issues.
- Expose a REST API alongside the Thymeleaf pages.
- Validate inputs and display useful error messages.

**Out of scope for MVP:** user accounts, comments, attachments, notifications, tagging, search across all projects, and advanced workflows. Security comes in the next release.

**Safety boundary:** The unauthenticated MVP is for local development only. Do not expose it publicly or deploy it with write access until security is implemented and tested.

## Planned stack

Java (class-required JDK), Spring Boot, Spring MVC, Spring Data JPA, Thymeleaf, Bean Validation, MySQL, Maven, Git/GitHub. Later: Spring Security, JWT, Ubuntu deployment. Choose exact framework versions together during setup.

## Documentation map

1. [Project charter](docs/01-project-charter.md) — purpose, users, boundaries, success criteria.
2. [Requirements and user stories](docs/02-requirements.md) — what the application must do.
3. [Architecture and API](docs/03-architecture-and-api.md) — technical layout, entities, routes, API contracts.
4. [Milestones and starter backlog](docs/04-milestones-and-backlog.md) — suggested implementation order and issue tickets.
5. [Team workflow](docs/05-team-workflow.md) — branch, PR, review, and testing rules.
6. [Decision record](docs/decisions/0001-initial-scope.md) — initial decisions and remaining approvals.
7. [Local MySQL setup](docs/development/local-mysql-setup.md) — native MySQL installation and configuration for both developers.

## Getting started (planning stage)

1. Read the project charter together and agree on the MVP boundary.
2. Resolve the open questions in the decision record.
3. Create a GitHub repository and add these files.
4. Convert the starter backlog into GitHub Issues, then assign owners.
5. Follow the [local MySQL setup guide](docs/development/local-mysql-setup.md) independently on each computer.
6. Create the Spring Boot skeleton after agreeing on the remaining API decisions.

> **Course note:** The course outline restricts AI-tool use for graded work. Keep this independent exercise separate from submissions, and check instructor rules before reusing any materials.
