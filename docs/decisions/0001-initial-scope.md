# ADR-0001 — Initial Scope and Architectural Starting Point

**Status:** Proposed — both developers should approve before coding  
**Date:** 2026-10-29

## Context

We want an independent two-person practice application covering the Spring Boot, Thymeleaf, REST, JPA, Spring Security, JWT, and Ubuntu deployment topics from PROG1346A. We need to avoid overbuilding before core workflows work.

## Proposed decisions

1. Build a **single Spring Boot application**, rather than microservices.
2. Deliver **projects + issues** first; no authentication until the next phase, with local-only access meanwhile.
3. Use **Thymeleaf for HTML** and **REST controllers for JSON**, both calling shared services.
4. Use **MySQL with Spring Data JPA**, starting with a `Project` 1→many `Issue` relationship.
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

## Decisions to confirm at the kickoff

- [ ] Keep **Mini Issue Tracker** as name, or rename it to: __________
- [ ] Agree on **single repository** (recommended) and repository owner: __________
- [ ] Decide default package name (e.g., `ca.example.miniissuetracker`): __________
- [ ] Choose local **MySQL installation vs container**: __________
- [ ] Agree on API prefix (`/api` proposed) and error response shape: __________
- [ ] Choose intended first demo date (optional): __________
- [ ] Agree on working/session cadence and initial issue leads: __________
- [ ] Confirm both developers approve MVP / deferred features: __________

## Approval

- Developer A: __________ / Date: __________
- Developer B: __________ / Date: __________
