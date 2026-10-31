# Application conventions (SET-03)

This is the shared implementation convention for feature work, following ADR-0001's single application, shared services, `/api` prefix, and RFC 9457 API errors. SET-03 depends only on SET-01 and requires no database. The older architecture sketch's field lists and domain behavior remain proposals where superseded by accepted ADRs; use ADR-0002 for enum and schema choices.

## Packages and boundaries

All packages live under `ca.humanbeans.sourbeans`. Create feature classes in these packages as needed; do not add placeholder services or entities.

| Package          | Responsibility                                                                                                                       |
| ---------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `controller.api` | `@RestController` JSON routes under `/api`; validate requests, call services, map results, set HTTP status and Location headers      |
| `controller.web` | `@Controller` Thymeleaf routes; bind forms, display validation messages, call the same services, redirect after successful mutations |
| `service`        | Business operations and transaction boundaries; enforce domain rules and throw domain exceptions; no HTTP or template concerns       |
| `repository`     | Spring Data persistence access; called by services, never directly by controllers                                                    |
| `entity`         | JPA mappings and persistence state; never used as a request body or serialized API response                                          |
| `dto`            | Immutable request/response records and explicit entity-to-DTO mapping helpers                                                        |
| `config`         | Framework configuration; no business logic or credentials in source                                                                  |
| `exception`      | Shared domain exceptions and API exception advice                                                                                    |

Thymeleaf templates live under `src/main/resources/templates`; static assets under `static`. API advice is scoped to `controller.api` (including subpackages), so HTML form errors remain part of MVC rendering. Keep web controllers out of that package. Do not add an API controller that serves an HTML route.

## DTO and mapping conventions

- Name input records by operation: `CreateProjectRequest`, `UpdateProjectRequest`; name output records by resource: `ProjectResponse`. These are naming examples, not implemented feature contracts.
- Include only client-editable fields in requests. IDs, persistence relationships, ownership and server timestamps belong in responses, not writable input fields.
- API controllers map request fields into service arguments, then map returned domain objects into response records. Keep mapping explicit; introduce a resource-specific mapper in `dto` when repeated. Mappers do not load data or call repositories. No mapping library is required.
- Services use domain types and ordinary Java arguments; they do not accept HTTP requests, ResponseEntity, BindingResult, or browser forms. Resolve related resources and business invariants in services.
- Do not serialize entities or return lazy relationships. Future entity mappings must ensure required data is loaded inside the service transaction before mapping.
- Define each feature's actual fields, enum defaults, limits and null semantics in its issue. Use the accepted schema/ADRs rather than copying illustrative older JSON examples.

## Validation

Put Jakarta Bean Validation annotations on request record components and use `@Valid @RequestBody` on API inputs. Use built-in MVC method validation (for example `@Positive` on an ID); do not put class-level `@Validated` on controllers. Missing bodies, parse failures, wrong types/enums and invalid inputs return 400. An ID's positive numeric format is separate from existence: services throw `ResourceNotFoundException` when a resource does not exist.

Use `@Valid @ModelAttribute` followed immediately by `BindingResult` for browser forms. On errors, re-render the form with field messages. Enforce business rules in services for both controller types. Handle domain failures explicitly in web controllers until shared HTML error pages are implemented.

Constraint messages must be static, client-safe descriptions. Do not interpolate rejected values, credentials, SQL, internal types or expressions into messages. Do not use validation for output/return constraints as a substitute for service correctness; MVC return-value validation failures are server errors.

## API error contract

`ApiExceptionHandler` extends Spring MVC's `ResponseEntityExceptionHandler`. Errors raised while invoking matched API controllers use `Content-Type: application/problem+json` and these standard members:

```json
{
    "type": "about:blank",
    "title": "Bad Request",
    "status": 400,
    "detail": "Request validation failed.",
    "instance": "/api/projects",
    "errors": [{ "field": "name", "message": "must not be blank" }]
}
```

The explicit SET-03 extension is `errors`: a sorted, deduplicated array of `{field, message}` for request-body field constraint failures. It never contains rejected values. Other errors omit it. MVC method-parameter validation uses the standard five fields with a generic 400 detail. `instance` is the request path, excluding query parameters. Titles follow the HTTP status reason phrase.

| Condition                                                                           | Status                             | Detail policy                                                                    |
| ----------------------------------------------------------------------------------- | ---------------------------------- | -------------------------------------------------------------------------------- |
| Invalid request body fields                                                         | 400                                | `Request validation failed.` plus field errors                                   |
| Malformed/missing body, invalid enum/type, missing parameter, parameter constraints | 400                                | Generic invalid/malformed request message                                        |
| `ResourceNotFoundException`                                                         | 404                                | Generic missing-resource message                                                 |
| `ResourceConflictException`                                                         | 409                                | Generic conflict with resource state                                             |
| Unsupported method/content type                                                     | 405 / 415                          | Safe generic detail; preserve protocol headers such as Allow                     |
| Unexpected exception or framework server error                                      | 500 (or framework's server status) | Generic server-error message; no exception message, stack trace or internal data |

The shared domain exceptions have no arbitrary client-message constructor. Do not expose raw persistence exception messages or map every database integrity failure to a client conflict. Future resource-specific safe messages must be deliberately designed and tested. Unexpected API failures log only the exception class, not arbitrary exception/request contents.

The main advice covers matched API controllers. `ApiRoutingExceptionHandler` handles pre-controller routing errors (missing routes, unsupported methods/media) only for `/api` and `/api/…`, delegating to the same response builder. Other paths retain normal MVC behavior. Servlet-container `/error` dispatches and authentication/authorization handling are separate future work. Do not enable global Problem Details auto-configuration alongside this advice without checking HTML routes.

## Verification

Run `.\mvnw.cmd clean verify` on Windows or `./mvnw clean verify` on Linux. Tests use test-only controllers and verify the actual MVC advice, Bean Validation, status codes, problem fields, no sensitive response details, and preservation of successful responses and Allow headers. No example endpoint ships in the application.

References: [Spring MVC Problem Details](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-rest-exceptions.html), [MVC validation](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-validation.html).
