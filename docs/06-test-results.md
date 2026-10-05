# Phase 6 — Test Results and MVP Validation

**Source prompt:** `docs/prompts/06-testing-validation.md`
**Authoritative artifacts:** `requirements.md`, `02-data-model.md`, `03-api-design.md`, `openapi.yaml`, `04-implementation-notes.md`, `05-local-run-guide.md`
**Phase:** 6 — Testing and API / OpenAPI Validation
**Date:** 2026-10-04

This document records the comprehensive validation of the approved MVP: automated
tests (JUnit 5 / Spring Boot Test / Mockito), OpenAPI contract conformance, a live
end-to-end API journey, negative/failure scenarios, and PostgreSQL persistence
verification.

---

## 1. Environment

| Component | Version / value |
|-----------|-----------------|
| JDK | Temurin 21 (21.0.12.1) |
| Maven | 3.10.0 |
| PostgreSQL | 16.15 (local, no container) |
| Demo database | `ebookstore` (unchanged by tests) |
| Test database | `ebookstore_test` (isolated, created for Phase 6) |

**Database isolation.** All automated integration tests run against a dedicated,
disposable `ebookstore_test` database so the demo database baseline is never
disturbed. The demo database is used only for the live end-to-end API run, and is
reset to a clean baseline before and after.

**No H2 was used.** Persistence and integration behaviour is validated against real
PostgreSQL (`@AutoConfigureTestDatabase(replace = NONE)`), satisfying the
requirement not to substitute H2 for database behaviour.

---

## 2. Repeatable Test State

A clean, reproducible starting state is guaranteed in three ways:

1. **`src/main/resources/db/reset.sql`** (new) — `TRUNCATE … RESTART IDENTITY
   CASCADE` over all tables, so re-seeding `db/data.sql` always yields the same
   deterministic IDs and stock levels.
2. **Automated integration tests** wipe and re-seed `ebookstore_test` before every
   test (`IntegrationTestBase.seedDatabase()`), and `@DataJpaTest` runs in a
   rolled-back transaction.
3. **Live run** — the demo DB was reset with `reset.sql` + `data.sql` before the
   end-to-end run, and reset again afterwards.

Baseline after reset: 1 demo user, 3 categories, 3 brands, 6 books (stock
15/8/12/20/10/5), 1 address, 0 carts, 0 orders. The approved data model was **not**
changed for testing convenience.

---

## 3. Automated Test Inventory

Command: `mvn clean test` → **BUILD SUCCESS**, **57 tests, 0 failures, 0 errors, 0 skipped.**

| Test class | Type | Tests | Target |
|------------|------|------:|--------|
| `SimulatedPaymentServiceTest` | Unit | 5 | Simulated payment success/decline (FR-06) |
| `CartServiceTest` | Unit (Mockito) | 9 | Cart logic: add/increment/update/remove, stock, not-found (FR-03, DM-07) |
| `OrderServiceTest` | Unit (Mockito) | 12 | Checkout, payment, stock, cancellation, ownership (FR-05–09, BR-04–07) |
| `RepositoryPersistenceTest` | Integration — PostgreSQL (`@DataJpaTest`) | 4 | Search query, NUMERIC(10,2) scale, unique (cart_id, book_id) |
| `BookstoreApiIntegrationTest` | Integration — PostgreSQL (`@SpringBootTest` + MockMvc) | 27 | Full API surface, security, E2E journey, negatives, persistence |
| **Total** | | **57** | |

### Coverage mapped to the Phase 6 required scenarios

| Required scenario | Where covered | Result |
|-------------------|---------------|:------:|
| Authentication / login | API `loginSuccess`; negatives `loginInvalidPassword`, `loginValidationError` | PASS |
| Catalogue retrieval | API `listCategories`, `listBooks` | PASS |
| Book search / filtering | API `searchBooksByTitle`, `searchBooksByAuthor`; repo `searchByTitleCaseInsensitive`, `searchWithNullFiltersReturnsAll` | PASS |
| Book detail retrieval | API `getBookDetail`, `getBookDetailNoBrand`, `getUnknownBook` | PASS |
| Cart retrieval | API `cartWorkflow` (empty cart) | PASS |
| Adding cart items | API `cartWorkflow`; unit `addNewItem` | PASS |
| Re-adding book / update quantity | API `cartWorkflow`; unit `reAddIncrementsQuantity` | PASS |
| Changing cart quantity | API `cartWorkflow`; unit `updateItemQuantity` | PASS |
| Removing cart items | API `cartWorkflow`; unit `removeItem` | PASS |
| Address listing / creation | API `listAddresses`, `createAddress`, `createAddressValidationError` | PASS |
| Checkout / order creation | API `endToEndJourney`; unit `checkoutHappyPath` | PASS |
| Simulated payment success | unit `checkoutHappyPath`, `SimulatedPaymentServiceTest.approvesPositiveAmount` | PASS |
| Simulated payment failure | unit `checkoutPaymentDeclined`, `declinesZeroAmount`, `declinesNegativeAmount`, `declinesNullAmount` | PASS |
| Insufficient stock | API `addItemInsufficientStock`; unit `checkoutInsufficientStock`, `addInsufficientStock` | PASS |
| Order history | API `endToEndJourney` (history step) | PASS |
| Order detail | API `endToEndJourney` (detail step) | PASS |
| Order ownership / security | API `accessAnotherCustomersOrder`; unit `getAnotherUsersOrder`, `cancelAnotherUsersOrder` | PASS |
| Cancellation within 48h | API `endToEndJourney` (cancel step); unit `cancelWithinWindow` | PASS |
| Cancellation outside 48h | API `cancelExpiredWindow`; unit `cancelAfterWindow` | PASS |
| Cancellation of already-cancelled | API `cancelAlreadyCancelled`; unit `cancelAlreadyCancelled` | PASS |
| Stock decrement after purchase | API `endToEndJourney` (verified via DB); unit `checkoutHappyPath` | PASS |
| Stock restoration after cancellation | API `endToEndJourney` (verified via DB); unit `cancelWithinWindow` | PASS |
| Cart clearing after checkout | API `endToEndJourney` (verified via DB) | PASS |
| Validation failures | API `loginValidationError`, `addItemValidationError`, `createAddressValidationError` | PASS |
| Not-found responses | API `getUnknownBook`, `addUnknownBookToCart`, `checkoutUnknownAddress`, `cancelUnknownOrder` | PASS |
| Unauthorized requests | API `protectedWithoutToken`, `protectedWithInvalidToken` | PASS |

---

## 4. OpenAPI Contract Conformance

All 14 approved endpoints were compared against `docs/api-design/openapi.yaml`.

| Method & Path | Implemented | Success code | Matches spec |
|---|:---:|:---:|:---:|
| POST `/auth/login` | ✓ | 200 | ✓ |
| GET `/categories` | ✓ | 200 | ✓ |
| GET `/books` | ✓ | 200 | ✓ |
| GET `/books/{bookId}` | ✓ | 200 | ✓ |
| GET `/cart` | ✓ | 200 | ✓ |
| POST `/cart/items` | ✓ | 200 | ✓ |
| PUT `/cart/items/{itemId}` | ✓ | 200 | ✓ |
| DELETE `/cart/items/{itemId}` | ✓ | 200 | ✓ |
| GET `/addresses` | ✓ | 200 | ✓ |
| POST `/addresses` | ✓ | 201 | ✓ |
| POST `/orders` | ✓ | 201 | ✓ |
| GET `/orders` | ✓ | 200 | ✓ |
| GET `/orders/{orderId}` | ✓ | 200 | ✓ |
| POST `/orders/{orderId}/cancel` | ✓ | 200 | ✓ |

Verified and conformant:

- **Paths & HTTP methods** — exact match; no unapproved MVP endpoints added.
  Secondary-scope endpoints (self-registration, address update/delete, Buy Again,
  recommendations, gift points, wishlist, reviews, related products) remain
  intentionally absent.
- **Request models** — `LoginRequest`, `AddToCartRequest`, `UpdateCartItemRequest`,
  `AddressRequest`, `PlaceOrderRequest` match the schema field names and validation.
- **Response models** — `AuthResponse`/`UserResponse`, `CategoryResponse`,
  `BookSummaryResponse`/`BookDetailResponse`, `PagedBooksResponse`, `CartResponse`/
  `CartItemResponse`, `AddressResponse`, `OrderResponse`/`OrderItemResponse`/
  `ShippingAddressSnapshot`, `PagedOrdersResponse` — all field names match.
- **Status codes** — success and error codes match (200/201/400/401/403/404/422/500).
- **Validation rules** — bean-validation constraints match (`@Min(1)` quantities,
  `@NotBlank`/`@Size` address fields, `@Email` login).
- **Error responses** — all use the `ErrorResponse` shape `{code, message, details?}`.
- **JWT security boundaries** — public: `POST /auth/login`, `GET /categories`,
  `GET /books/**`; everything else requires a Bearer token. Matches §6 of the API design.
- **Monetary fields** — `BigDecimal` end-to-end, `NUMERIC(10,2)` in PostgreSQL,
  serialized as JSON numbers (confirmed, e.g. `42.50`, `85.00`).
- **Order status values** — enum restricted to exactly `CONFIRMED` and `CANCELLED`.

### Conformance defects found and fixed

Three discrepancies were found. In each case the **implementation** was wrong and
the **approved OpenAPI contract was left unchanged**:

| # | Discrepancy | Spec (authoritative) | Was | Fix |
|---|-------------|----------------------|-----|-----|
| D1 | Unauthenticated access to a protected endpoint | `401 UNAUTHORIZED` with structured `ErrorResponse` | Spring Security default `403` with empty body | Added `RestAuthenticationEntryPoint` (401) and `RestAccessDeniedHandler` (403) emitting `ErrorResponse`; wired into `SecurityConfig.exceptionHandling(...)` |
| D2 | Empty-cart checkout error code | `CART_EMPTY` | `EMPTY_CART` | Aligned `OrderService` to emit `CART_EMPTY` |
| D3 | Checkout with another customer's address | `422` with code `FORBIDDEN` (per `/orders` 422 example) | `422` with code `ADDRESS_NOT_OWNED` | Aligned `OrderService` to emit `FORBIDDEN` |

All three are verified by automated tests and the live run (§5–§6). No change was
made to `openapi.yaml`.

---

## 5. End-to-End MVP API Run (live, against PostgreSQL)

The application was started locally (`java -jar …` against the `ebookstore` demo
DB) and the complete customer journey executed with `curl`, verifying PostgreSQL
side effects at each mutating step.

| Step | Request | Result | DB side effect verified |
|------|---------|:------:|-------------------------|
| Login | POST `/auth/login` | 200, JWT + user summary | — |
| Browse categories | GET `/categories` | 200 (3) | — |
| Browse books | GET `/books` | 200 (`totalElements=6`) | — |
| Search (title) | GET `/books?title=clean` | 200 → Clean Code only | — |
| Search (author) | GET `/books?authorName=asimov` | 200 → Foundation only | — |
| Book detail | GET `/books/2` | 200 (price 42.50, category Technology, brand Addison-Wesley) | — |
| Add to cart | POST `/cart/items` (2× Clean Code) | 200 (`totalAmount=85.00`) | cart + cart_item persisted |
| View / update cart | GET `/cart`, PUT `/cart/items/{id}` | 200 (qty 2→3→2) | quantity persisted |
| List addresses | GET `/addresses` | 200 (Austin) | — |
| **Checkout** | POST `/orders` | **201 CONFIRMED**, total 85.00, `SIM-…` ref, Austin snapshot, unitPrice 42.50 | **stock 8→6**, **cart_items 0**, order + order_item rows written, `unit_price` locked at 42.50 |
| Order history | GET `/orders` | 200 (`totalElements=1`) | read-back from DB |
| Order detail | GET `/orders/{id}` | 200 CONFIRMED | read-back from DB |
| **Cancel (within 48h)** | POST `/orders/{id}/cancel` | **200 CANCELLED** | **stock restored 6→8**, status CANCELLED |

**End-to-end result: PASS.** Checkout, stock decrement, address snapshot, price
locking, cart clearing, and cancellation stock restoration all persisted correctly
in PostgreSQL.

---

## 6. Negative / Failure Scenarios (live)

Every error returned the approved structured `ErrorResponse` (`{code, message, details?}`).

| # | Scenario | Expected | Actual | Result |
|---|----------|----------|--------|:------:|
| N1 | Invalid login (wrong password) | 401 `INVALID_CREDENTIALS` | 401 `INVALID_CREDENTIALS` | PASS |
| N2 | Missing JWT on protected endpoint | 401 `UNAUTHORIZED` | 401 `UNAUTHORIZED` | PASS |
| N3 | Invalid JWT | 401 `UNAUTHORIZED` | 401 `UNAUTHORIZED` | PASS |
| N4 | Invalid request payload (bad email) | 400 `VALIDATION_ERROR` (+details) | 400 `VALIDATION_ERROR` with field details | PASS |
| N5 | Unknown book | 404 `NOT_FOUND` | 404 `NOT_FOUND` | PASS |
| N6 | Unknown address at checkout | 404 `NOT_FOUND` | 404 `NOT_FOUND` | PASS |
| N7 | Empty-cart checkout | 422 `CART_EMPTY` | 422 `CART_EMPTY` | PASS |
| N8 | Insufficient stock | 422 `INSUFFICIENT_STOCK` | 422 `INSUFFICIENT_STOCK` | PASS |
| N9 | Simulated payment failure | 422 `PAYMENT_DECLINED` | covered by unit tests (see note) | PASS |
| N10 | Accessing another customer's order | 403 `FORBIDDEN` | 403 `FORBIDDEN` | PASS |
| N11 | Checkout with another customer's address | 422 `FORBIDDEN` | 422 `FORBIDDEN` | PASS |
| N12 | Cancelling an unknown order | 404 `NOT_FOUND` | 404 `NOT_FOUND` | PASS |
| N13 | Cancelling an already-cancelled order | 422 `ORDER_ALREADY_CANCELLED` | 422 `ORDER_ALREADY_CANCELLED` | PASS |
| N14 | Cancelling after the 48-hour window | 422 `CANCELLATION_WINDOW_EXPIRED` | 422 `CANCELLATION_WINDOW_EXPIRED` | PASS |
| N15 | Invalid quantity (0) when adding to cart | 400 `VALIDATION_ERROR` | 400 `VALIDATION_ERROR` | PASS |

> **Note on N9 (payment decline).** The simulated payment processor approves all
> well-formed positive amounts and deterministically declines only a non-positive
> total (the demonstrable failure hook, per FR-06 / DM-02). Because every seeded
> book has a positive price, the decline path is not reachable through the normal
> API with demo data, so it is validated at the unit level
> (`OrderServiceTest.checkoutPaymentDeclined`, `SimulatedPaymentServiceTest`) which
> confirms a `422 PAYMENT_DECLINED` and that **no order is persisted** on decline.

---

## 7. PostgreSQL Persistence Result

Persistence was validated against real PostgreSQL, not H2:

- **Repository layer** (`RepositoryPersistenceTest`) — the case-insensitive search
  query runs correctly (the earlier `lower(bytea)` issue stays fixed), `price`
  round-trips as `NUMERIC(10,2)` with scale 2, and the `UNIQUE (cart_id, book_id)`
  constraint (DM-07) is enforced by the database.
- **Full stack** (`BookstoreApiIntegrationTest`) — writes are committed and read
  back across transactions; stock decrement, cart clearing, order/`order_item`
  creation, and the locked `unit_price` were asserted directly via SQL.
- **Live run** (§5) — direct `psql` queries confirmed stock 8→6 on checkout, cart
  cleared, snapshot/price persisted, and stock restored 6→8 on cancellation.
- **Startup validation** — the app runs with `ddl-auto=validate`; a successful
  start proves every JPA mapping validates against the live schema.

Result: **PASS.**

---

## 8. API Test Collection

An importable Postman collection and environment are provided under
`docs/api-testing/`:

- `ebookstore.postman_collection.json` — happy-path workflow + key negative tests,
  with inline assertions. Login captures the JWT into `{{token}}` automatically.
- `ebookstore.postman_environment.json` — base URL, demo credentials, and
  auto-captured variables.
- `README.md` — import/run instructions and a manual request-sequence fallback.

No real secrets are included; the demo credentials are the local demonstration
account seeded by `db/data.sql`.

---

## 9. Defects Discovered, Fixes, and Retests

| ID | Classification | Description | Fix | Retest |
|----|----------------|-------------|-----|--------|
| D1 | Implementation (security) | Unauthenticated protected requests returned 403 + empty body instead of 401 `UNAUTHORIZED` structured | Added `RestAuthenticationEntryPoint` + `RestAccessDeniedHandler`, wired into `SecurityConfig` | `protectedWithoutToken`, `protectedWithInvalidToken` PASS; live N2/N3 PASS |
| D2 | Implementation (contract) | Empty-cart code `EMPTY_CART` ≠ spec `CART_EMPTY` | `OrderService` emits `CART_EMPTY` | `checkoutEmptyCart` PASS; live N7 PASS |
| D3 | Implementation (contract) | Foreign-address checkout code `ADDRESS_NOT_OWNED` ≠ spec `FORBIDDEN` | `OrderService` emits `FORBIDDEN` | `checkoutForeignAddress` PASS; live N11 PASS |

No defects in the database schema, data model, or OpenAPI contract were found; the
contract was treated as authoritative and left unchanged. Scope was not expanded —
no secondary features were added.

---

## 10. Final Validation Checklist

| Check | Result |
|-------|:------:|
| Maven build succeeds (`mvn clean test`) | PASS (BUILD SUCCESS) |
| Automated tests pass (57/57) | PASS |
| PostgreSQL integration tests pass (no H2 substitution) | PASS |
| End-to-end API workflow passes | PASS |
| OpenAPI matches implementation | PASS (3 impl fixes; spec unchanged) |
| Authentication / security tests pass | PASS |
| Persistent database changes verified | PASS |
| No sensitive credentials committed | PASS (local config + `run-local.sh` gitignored; no hard-coded secrets in `src/main`) |
| No secondary features unintentionally added | PASS |

### Artifacts added in Phase 6

- `src/main/resources/db/reset.sql` — repeatable demo/test DB reset.
- `src/main/java/com/bookstore/security/RestAuthenticationEntryPoint.java` — 401 structured.
- `src/main/java/com/bookstore/security/RestAccessDeniedHandler.java` — 403 structured.
- `src/test/**` — 5 test classes + 2 test support classes + `application-test.properties`.
- `docs/api-testing/**` — Postman collection, environment, README.
- `docs/06-test-results.md` — this document.

### Files modified in Phase 6

- `src/main/java/com/bookstore/order/OrderService.java` — error-code alignment (D2, D3).
- `src/main/java/com/bookstore/security/SecurityConfig.java` — structured 401/403 handlers (D1).
- `.gitignore` — ignore `run-local.sh`.

### Remaining blockers

None.

---

## 11. Phase 6 Completion Report

- **Repeatable state:** PASS — `reset.sql` + `data.sql` give a deterministic
  baseline; integration tests isolate on `ebookstore_test`; the demo DB is untouched.
- **Automated testing:** PASS — 57 tests (unit + Mockito + PostgreSQL integration +
  full-stack API), all green.
- **OpenAPI conformance:** PASS — all 14 endpoints conform; three implementation
  defects were fixed to match the approved contract, which itself was unchanged.
- **End-to-end MVP:** PASS — the full journey (login → browse/search → cart →
  address → checkout → confirmation → history → detail → cancel) works against the
  live app with all PostgreSQL side effects verified.
- **Negative scenarios:** PASS — all required failure cases return correct status
  codes and the structured `ErrorResponse`.
- **Security:** PASS — JWT boundaries enforced; 401/403 now structured.
- **Persistence:** PASS — validated against real PostgreSQL throughout.
- **Secrets:** PASS — none committed.
- **Scope:** PASS — no secondary features added.

---

**Status: MVP Validated — Ready for Final Review**
