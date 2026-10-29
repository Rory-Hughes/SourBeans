# ADR-0001 — Initial Scope and Architectural Starting Point

**Status:** Proposed — overall architecture still requires both developers' approval. Local MySQL development choice confirmed by Rory (2026-10-29); Josh's review remains pending.  
**Date:** 2026-10-29

## Context

We want an independent two-person practice application covering the Spring Boot, Thymeleaf, REST, JPA, Spring Security, JWT, and Ubuntu deployment topics from PROG1346A. We need to avoid overbuilding before core workflows work.

## Proposed decisions

1. Build a **single Spring Boot application**, rather than microservices.
2. Deliver **projects + issues** first; no authentication until the next phase, with local-only access meanwhile.
3. Use **Thymeleaf for HTML** and **REST controllers for JSON**, both calling shared services.
4. Use **MySQL with Spring Data JPA**, starting with a `Project` 1→many `Issue` relationship. Each developer will install **MySQL Community Server 8.4 LTS locally (without Docker)**, with an independent database. See [local MySQL setup](../development/local-mysql-setup.md).
5. Treat **comments, assignees, attachments, notifications** as stretch goals.
6. Add **Spring Security web sessions + JWT-protected REST API** once the MVP CRUD features pass tests.
7. Deploy **only after** securing the app, ideally using a managed service on an Ubuntu VM.
8. Use GitHub Issues + reviewed PRs with rotating responsibilities.

## Why

These choices provide practice with the course's core technologies and keep responsibilities manageable for two developers. Separate controllers sharing services minimize duplicated business logic and enable security checks in one place.

## Consequences / trade-offs

- The first stage has no accounts and must not be network-exposed.
- Adding users/memberships later requires schema evolution and authorization tests.
- Supporting both session-based browser login and JWT-secured APIs adds complexity but increases learning value.
- A small domain limits feature breadth, which is deliberate.
- Each developer maintains their own native MySQL service, database, and local credentials; the team targets the same MySQL 8.4 LTS release family.
- No shared remote development database. Schema changes must be version-controlled and reproducible (migration mechanism still to be decided).
- MySQL is bound to loopback for local development; Spring Boot must use a non-root DB user and credentials must never be committed. Development-only privileges must not be carried into production.

## Decisions to confirm at the kickoff

- [x] Keep **Mini Issue Tracker** as name, or rename it to: SourBeans.
- [x] Agree on **single repository** and repository owner: Rory Hughes
- [x] Decide default package name (e.g., `ca.example.miniissuetracker`): ca.example.sourbeans
- [x] Choose local **MySQL installation vs container**: **Native local installation, MySQL Community Server 8.4 LTS, separately on both machines; no Docker.** [Setup instructions](../development/local-mysql-setup.md). Rory confirmed on 2026-10-29; pending Josh's review.
- [ ] Agree on API prefix (`/api` proposed) and error response shape: __________
- [ ] Choose intended first demo date (optional): __________
- [ ] Agree on working/session cadence and initial issue leads: __________
- [ ] Confirm both developers approve MVP / deferred features: __________

## Approval

**Database choice:** Rory confirmed local MySQL on 2026-10-29. Josh's review remains pending.

**Overall ADR approval (not implied by this database decision):**

- Rory : __________ / Date: __________
- Josh : __________ / Date: __________
