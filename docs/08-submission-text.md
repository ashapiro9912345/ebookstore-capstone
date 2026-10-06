# Phase 8 — AI Review Submission: Text Response

**How to use this file:** paste the public GitHub URL in place of `GITHUB_REPO_URL`
below, then copy everything between the `=== BEGIN ===` / `=== END ===` markers into
the submission's text field. The body is under the 4000-character limit (see count at
the bottom).

Your repo (make it public first — see `docs/08-submission-checklist.md`):
`https://github.com/ashapiro9912345/ebookstore-capstone`

---

=== BEGIN SUBMISSION TEXT ===

E-Bookstore Backend — AI Specialist Capstone

GitHub: GITHUB_REPO_URL

Summary
I built the backend for an e-commerce bookstore entirely inside an agentic IDE, AWS Kiro, using a disciplined, phase-by-phase workflow. The goal was not just a working app but a traceable AI-assisted process. Stack: Java 21, Spring Boot 3.3.4, Maven, PostgreSQL 16, Spring Data JPA/Hibernate, JWT (jjwt) with BCrypt, and JUnit 5 / Spring Boot Test.

Agentic IDE workflow
I drove Kiro one prompt per phase and reviewed each artifact before moving on, following this sequence: Requirements -> Data Model -> OpenAPI -> Implementation -> PostgreSQL -> Testing -> Final Review. Every phase's prompt and output is kept in the repo (docs/prompts/ and the docs/ artifacts) so the whole process is auditable, not just the final code. Kiro explained intended changes, surfaced assumptions and ambiguities, and identified affected files before generating significant code.

Core deliverable: API design
The API contract is an OpenAPI 3.x spec (docs/api-design/openapi.yaml) defining all 14 MVP endpoints across auth, catalogue, cart, addresses, and orders. The implementation was built to match the contract, and I verified conformance across every endpoint; where the implementation disagreed, I fixed the code and left the approved contract unchanged.

Working application
The app runs locally against PostgreSQL and performs the full MVP journey: login (JWT) -> browse/search books -> manage cart -> select address -> checkout with simulated payment -> order confirmation -> order history -> cancellation within 48 hours. I verified real persistence: at checkout, book stock decrements and the cart clears; on cancellation within the window, stock is restored. Orders store an address snapshot and lock the unit price at purchase time.

Best practices
- Layered modular monolith: Controller -> Service -> Repository -> PostgreSQL.
- DTOs kept separate from JPA entities.
- Stateless JWT security; BCrypt-hashed passwords.
- Structured error handling with a consistent { code, message, details? } body and correct HTTP status codes (400/401/403/404/422/500).
- 57 automated tests pass (mvn clean test -> BUILD SUCCESS), including integration tests against real PostgreSQL (no H2 substitution).
- No secrets committed; DB and JWT credentials are supplied via environment variables.

Simulated payment
Payment is simulated for demonstration only — no real processor is connected and no card numbers or CVVs are ever stored; only a simulated reference is recorded.

Scope and deployability
The app is designed to run locally via documented environment variables (DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET). Cloud hosting, containers, and microservices were intentionally kept out of scope to focus on a correct, simple, well-tested MVP.

The repository contains the full source, the OpenAPI contract, an importable Postman collection, per-phase documentation, and the prompts used to drive Kiro.

=== END SUBMISSION TEXT ===

---

**Character-count note:** The text between the BEGIN/END markers (with the
`GITHUB_REPO_URL` placeholder) is ~3,000 characters — comfortably under the
4,000-character limit. Pasting a typical GitHub URL (~55 chars) in place of the
placeholder keeps it well under. To confirm after you paste your URL:

```bash
# counts characters of the submission body only (between the markers)
awk '/=== BEGIN SUBMISSION TEXT ===/{f=1;next}/=== END SUBMISSION TEXT ===/{f=0}f' \
  docs/08-submission-text.md | wc -m
```
