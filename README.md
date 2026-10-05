# E-Bookstore Backend — AI Specialist Capstone

Backend REST API for an e-commerce bookstore, built as the AI Specialist Capstone
Project using a disciplined, AI-assisted development workflow with **AWS Kiro** as
the agentic development environment.

The application runs locally against PostgreSQL and implements the complete MVP
customer journey: browse → cart → checkout (simulated payment) → order history →
cancellation.

---

## Project Overview

This repository contains a modular monolithic Spring Boot backend developed phase by
phase with AWS Kiro, from requirements analysis through to tested implementation. The
goal of the capstone is to demonstrate not only a working application but a
traceable, AI-assisted software development process.

Each phase is documented under `docs/` and the design artifacts (requirements, data
model, API design, OpenAPI contract) are treated as authoritative inputs to the
implementation.

---

## Technology Stack

- **Java 21** (Temurin)
- **Spring Boot 3.x** (3.3.4)
- **Maven**
- **PostgreSQL** (16.x)
- **Spring Data JPA / Hibernate**
- **REST APIs**
- **OpenAPI 3.x** (API contract)
- **JWT authentication** (jjwt), BCrypt password hashing
- **JUnit 5 / Spring Boot Test** (with Mockito)
- **Postman** (importable API test collection)
- **Git / GitHub**

---

## Architecture

A simple layered architecture inside a modular monolith:

```
Controller → Service → Repository → PostgreSQL
```

- **DTO / entity separation** — controllers speak only in DTOs; hand-written mappers
  convert between JPA entities and request/response DTOs.
- **Modular monolith** — code is organized by business capability
  (`auth`, `catalogue`, `cart`, `address`, `order`, `user`, `security`, `common`)
  while preserving the single layered flow above.
- **Stateless JWT security** — public catalogue/login endpoints; all customer
  operations require a Bearer token.
- **Structured error handling** — a global exception handler returns a consistent
  `{ code, message, details? }` error body with appropriate HTTP status codes.

---

## MVP Capabilities

- Customer **login** (JWT)
- **Category** and **book browsing**
- **Search / filtering** (by title, author, category, brand)
- **Shopping cart** management (add, update quantity, remove; one reusable cart per
  customer; quantity increments on re-add)
- **Delivery address** listing and creation
- **Checkout** (order creation)
- **Simulated payment** (demonstration only — no real processor, no card data stored)
- **Order confirmation** (with address snapshot and price locked at purchase time)
- **Order history** and **order detail**
- **Order cancellation within 48 hours** (restores stock)

> Secondary features (self-registration, Buy Again, recommendations, gift points,
> wishlist, reviews, related products, address edit/delete) are intentionally **not**
> implemented — they are out of the approved MVP scope.

---

## Local Prerequisites

- **Java 21** (JDK)
- **Maven**
- **PostgreSQL 16** (or a compatible PostgreSQL version), running locally

---

## Local Setup

Full step-by-step instructions are in **[`docs/05-local-run-guide.md`](docs/05-local-run-guide.md)**.
In summary:

1. Create the `ebookstore` role and database.
2. Apply the schema: `src/main/resources/db/schema.sql`.
3. Load demo data: `src/main/resources/db/data.sql`.
   (To reset to a clean, repeatable state, run `src/main/resources/db/reset.sql`
   first, then re-apply `data.sql`.)

### Required environment variables

Credentials are never hard-coded or committed; the application reads them from the
environment (local dev defaults exist only for convenience):

| Variable | Purpose |
|----------|---------|
| `DB_URL` | JDBC URL, e.g. `jdbc:postgresql://localhost:5432/ebookstore` |
| `DB_USERNAME` | Database role (e.g. `ebookstore`) |
| `DB_PASSWORD` | Database password — **set this yourself; never commit it** |
| `JWT_SECRET` | Base64-encoded 32-byte JWT signing key |
| `JWT_EXPIRATION_MS` | (optional) token lifetime, default 86400000 |

---

## Build

Run the full build with tests (requires a local PostgreSQL `ebookstore_test`
database — see `docs/05-local-run-guide.md` and `docs/06-test-results.md`):

```bash
mvn clean test
```

Package the runnable jar:

```bash
mvn clean package
```

---

## Run

With PostgreSQL running and the schema + demo data loaded:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export DB_URL="jdbc:postgresql://localhost:5432/ebookstore"
export DB_USERNAME="ebookstore"
export DB_PASSWORD="your_local_password"
export JWT_SECRET="your_base64_32_byte_secret"

java -jar target/ebookstore-1.0.0-SNAPSHOT.jar
```

The API base URL is `http://localhost:8080/api`.

---

## API Documentation

The approved API contract is the OpenAPI specification:

**[`docs/api-design/openapi.yaml`](docs/api-design/openapi.yaml)**

The implementation is kept consistent with this contract (verified in Phase 6).

---

## API Testing

An importable Postman collection and environment are provided under
**[`docs/api-testing/`](docs/api-testing/)**:

- `ebookstore.postman_collection.json`
- `ebookstore.postman_environment.json`
- `README.md` (import and run instructions, plus a manual request-sequence fallback)

Import both JSON files into Postman, select the **E-Bookstore Local** environment,
and run the folders in order. The **Login** request stores the JWT automatically, so
protected requests send `Authorization: Bearer {{token}}` without manual copying.

---

## Demo Account

For local demonstration only, a demo customer is seeded by `db/data.sql`:

- **Email:** `demo@bookstore.com`
- **Password:** `demo1234`

> **Local/demo credentials only.** This account exists purely for the capstone
> demonstration against a local database. It is not a real credential and must not
> be reused anywhere real.

---

## Testing Evidence

Phase 6 validation (details in **[`docs/06-test-results.md`](docs/06-test-results.md)**):

- **57 automated tests, 0 failures** (`mvn clean test` → BUILD SUCCESS)
- **Real PostgreSQL integration testing** (no H2 substitution) for repository and
  full-stack API behaviour
- **Successful end-to-end MVP validation** against the running application, with
  PostgreSQL side effects (stock decrement/restore, cart clearing, order creation)
  verified directly
- OpenAPI contract conformance confirmed across all 14 endpoints

---

## AI-Assisted Development Process

The project followed a disciplined, phase-by-phase sequence driven with AWS Kiro,
with each phase reviewed before proceeding:

```
Requirements → Data Model → OpenAPI → Implementation → PostgreSQL → Testing → Final Review
```

Supporting documentation per phase lives in `docs/`:

| Phase | Artifact |
|-------|----------|
| 1 — Requirements | `docs/requirements/requirements.md` |
| 2 — Data Model | `docs/data-model/02-data-model.md` |
| 3 — API Design / OpenAPI | `docs/api-design/03-api-design.md`, `docs/api-design/openapi.yaml` |
| 4 — Implementation | `docs/04-implementation-notes.md` |
| 5 — PostgreSQL Local Run | `docs/05-local-run-guide.md` |
| 6 — Testing & Validation | `docs/06-test-results.md` |
| 7 — Final Review & Demo | `docs/07-demo-guide.md`, `docs/07-final-checklist.md` |

The per-phase prompts used to drive the work are kept under `docs/prompts/` for
traceability.

---

## Repository Layout

```
ebookstore-capstone/
├── README.md
├── pom.xml
├── docs/
│   ├── requirements/requirements.md
│   ├── data-model/02-data-model.md
│   ├── api-design/{03-api-design.md, openapi.yaml}
│   ├── api-testing/{collection, environment, README}
│   ├── prompts/
│   ├── 04-implementation-notes.md
│   ├── 05-local-run-guide.md
│   ├── 06-test-results.md
│   ├── 07-demo-guide.md
│   └── 07-final-checklist.md
└── src/
    ├── main/java/com/bookstore/...        # Controller → Service → Repository
    ├── main/resources/
    │   ├── application.properties
    │   └── db/{schema.sql, data.sql, reset.sql}
    └── test/java/com/bookstore/...        # JUnit 5 + Spring Boot Test (PostgreSQL)
```
