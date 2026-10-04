# Prompt 4 — Generate the Spring Boot Backend

Act as a Senior Java 21 / Spring Boot 3.x Developer.

Use the approved:
- `requirements.md`
- `02-data-model.md`
- `03-api-design.md`
- `openapi.yaml`

Implement the bookstore backend as a modular monolithic Spring Boot application.

## Technology

Use:

- Java 21
- Spring Boot 3.x
- Maven
- Spring Data JPA / Hibernate
- PostgreSQL
- Jakarta Bean Validation
- Spring Security using the simplest approved authentication design
- JUnit 5 / Spring Boot Test

Do not introduce:
- microservices
- Docker
- Kubernetes
- AWS hosting
- external payment processors
- unnecessary frameworks

## Architecture

Use:

Controller → Service → Repository → PostgreSQL

Keep DTOs separate from JPA entities.

Organize code by logical business capability where practical while preserving a simple layered architecture.

## Implement

Generate the application structure and required code for:

1. Maven project configuration
2. Application configuration structure
3. JPA entities based on the approved data model
4. Spring Data repositories
5. Request/response DTOs
6. Mapping logic
7. Service-layer business logic
8. REST controllers matching `openapi.yaml`
9. Validation
10. Authentication/security
11. Global exception handling
12. Structured error responses
13. Logging appropriate for local development
14. Simulated payment behavior
15. 48-hour order cancellation rule
16. Historical order data preservation
17. Stock validation and decrement on successful purchase

## Implementation Rules

- Follow `openapi.yaml` as the API contract.
- Do not invent new endpoints without documenting why.
- Do not store card numbers, CVVs, expiration dates, or real payment credentials.
- Do not place secrets or database passwords in source code.
- Do not implement Secondary features unless already approved.
- Prefer simple handwritten mappings over adding a mapping framework unless that framework clearly reduces complexity.
- Prefer the minimum dependencies required to complete the capstone.

## Before Changing Files

Briefly state:
- what will be created,
- important assumptions,
- files/packages affected,
- any architectural decision that differs from the approved design.

Then perform the implementation.

## Output

Create the complete Spring Boot backend in the project.

Also create/update:

`04-implementation-notes.md`

Document:
- package structure
- major design decisions
- any deviations from the approved OpenAPI/data model
- remaining issues

End with:

**Status: Implementation Complete — Awaiting Database Configuration and Local Run**
