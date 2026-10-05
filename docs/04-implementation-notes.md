# Phase 4 — Spring Boot Implementation Notes

**Source prompt:** `docs/prompts/04-spring-boot-implementation.md`
**Authoritative artifacts:** `requirements.md`, `02-data-model.md`, `03-api-design.md`, `openapi.yaml`
**Governing standards:** Kiro Project Instructions
**Phase:** 4 — Spring Boot Implementation (recovered after an interrupted run)

---

## 1. Recovery Summary

Phase 4 was interrupted partway through. This run assessed the existing project
state, compared it against the approved artifacts, and resumed from the first
incomplete item without regenerating working components.

### What already existed before recovery (left untouched)

- Maven project (`pom.xml`) — Spring Boot 3.3.4 parent, Java 21, all required dependencies
  (web, data-jpa, security, validation, PostgreSQL driver, jjwt 0.12.6, test).
- `application.properties` and `application-local.properties` — datasource, JPA
  (`ddl-auto=validate`), JWT, and logging configured via environment-variable placeholders.
  No secrets in source.
- All 9 JPA entities + 2 enums: `User`, `Category`, `Brand`, `Book`, `Address`,
  `Cart`, `CartItem`, `Order`, `OrderItem`, `OrderStatus`, `PaymentMethod`.
- All 7 Spring Data repositories with the custom query methods needed by the MVP.
- 3 DTOs: `UserResponse`, `AuthResponse`, `ErrorResponse`.

### What was incomplete / missing (built in this recovery)

The entire stack above the repository layer was absent:

- Security layer (JWT service, filter, user-details service, Spring Security config).
- All request DTOs and the remaining response DTOs.
- Hand-written entity → DTO mappers.
- All service-layer business logic.
- All REST controllers.
- Custom exceptions and the global exception handler.
- Simulated payment logic.
- Checkout, order history/detail, and 48-hour cancellation logic.

### What was completed or corrected in this recovery

New packages and classes created:

**security/**
- `JwtService` — issues/validates HS256 JWTs with jjwt 0.12.x; secret from `JWT_SECRET`.
- `UserDetailsServiceImpl` — loads `User` by email as the Spring Security principal.
- `JwtAuthenticationFilter` — `OncePerRequestFilter`; populates the security context from a valid Bearer token.
- `SecurityConfig` — stateless filter chain; `BCryptPasswordEncoder`; public vs. protected endpoint rules.

**user/**
- `CurrentUserService` — resolves the authenticated `User` entity from the security context.

**auth/**
- `AuthService`, `AuthController` — `POST /api/auth/login`.

**catalogue/**
- `CatalogueMapper`, `CategoryService`, `BookService`, `CategoryController`, `BookController`.

**address/**
- `AddressMapper`, `AddressService`, `AddressController`.

**cart/**
- `CartMapper`, `CartService`, `CartController`.

**order/**
- `OrderMapper`, `SimulatedPaymentService`, `OrderService`, `OrderController`.

**common/exception/**
- `ResourceNotFoundException`, `BusinessRuleException`, `GlobalExceptionHandler`.

Plus all request/response DTOs under the respective `*/dto` packages.

---

## 2. Package Structure

```
com.bookstore
├── EbookstoreApplication
├── auth
│   ├── AuthController, AuthService
│   └── dto/LoginRequest
├── user
│   ├── User, UserRepository, CurrentUserService
│   └── dto/UserResponse, AuthResponse
├── catalogue
│   ├── Book, Category, Brand (+ repositories)
│   ├── BookService, CategoryService, CatalogueMapper
│   ├── BookController, CategoryController
│   └── dto/BookSummaryResponse, BookDetailResponse,
│           CategoryResponse, BrandResponse, PagedBooksResponse
├── address
│   ├── Address, AddressRepository
│   ├── AddressService, AddressMapper, AddressController
│   └── dto/AddressRequest, AddressResponse
├── cart
│   ├── Cart, CartItem (+ repositories)
│   ├── CartService, CartMapper, CartController
│   └── dto/AddToCartRequest, UpdateCartItemRequest,
│           CartItemResponse, CartResponse
├── order
│   ├── Order, OrderItem, OrderStatus, PaymentMethod (+ repository)
│   ├── OrderService, OrderMapper, SimulatedPaymentService, OrderController
│   └── dto/PlaceOrderRequest, OrderItemResponse, OrderResponse,
│           ShippingAddressSnapshot, PagedOrdersResponse
├── security
│   ├── JwtService, JwtAuthenticationFilter
│   ├── UserDetailsServiceImpl, SecurityConfig
└── common
    ├── dto/ErrorResponse
    └── exception/ResourceNotFoundException, BusinessRuleException,
                   GlobalExceptionHandler
```

Code is organised by business capability while preserving a simple layered
architecture: **Controller → Service → Repository → PostgreSQL**.

---

## 3. Major Design Decisions (all align with approved artifacts)

- **Layering & DTO boundary.** Controllers speak only in DTOs. Hand-written mappers
  (`CatalogueMapper`, `AddressMapper`, `CartMapper`, `OrderMapper`) convert between
  entities and DTOs. No mapping framework was added (simplicity over tooling).
- **Authentication.** JWT Bearer, stateless session policy, BCrypt password hashing —
  as decided in `03-api-design.md`. The principal is the customer's email.
- **Endpoint security** matches `03-api-design.md` §6: `POST /api/auth/login`,
  `GET /api/categories`, and `GET /api/books/**` are public; everything else requires a token.
- **Checkout = order creation (DM-02).** `POST /api/orders` performs address validation,
  stock re-check, simulated payment, order creation, stock decrement, and cart clearing
  in a single transaction. There is no separate payment entity or payment endpoint.
- **Simulated payment (FR-06).** `SimulatedPaymentService` returns an approval flag and a
  generated `SIM-XXXXXXXX` reference. It never accepts or stores card numbers, CVVs,
  expiry dates, or real tokens. A non-positive amount deterministically declines so the
  `422 PAYMENT_DECLINED` path is demonstrable.
- **Address snapshot (DM-03).** Orders store the delivery address in `snap_*` columns at
  creation time; `OrderResponse.shippingAddress` is built from the snapshot, not the live address.
- **Historical price (BR-06).** `OrderItem.unitPrice` is locked to the book's price at checkout.
- **Order lifecycle (DM-08).** Orders are created `CONFIRMED`; the only transition is
  `CONFIRMED → CANCELLED` via `POST /api/orders/{orderId}/cancel`.
- **48-hour cancellation (BR-07).** Cancellation is allowed only within 48 hours of
  `created_at` and only when the order is `CONFIRMED`; cancellation restores stock for all items.
- **One reusable cart per customer (DM-06).** The cart is created on first use and reused;
  items are cleared after a successful checkout.
- **No duplicate books in a cart (DM-07).** Adding an existing book increments the quantity
  on the existing row.
- **Monetary values** use `BigDecimal` throughout (prices, line totals, order totals).
- **Error handling.** `GlobalExceptionHandler` maps exceptions to the status codes defined in
  `03-api-design.md` §7: 400 (validation), 401 (bad credentials), 403 (another user's resource),
  404 (not found), 422 (business rule), 500 (unexpected). Responses use the `ErrorResponse` shape.

---

## 4. Validation

Jakarta Bean Validation on request DTOs:

- `LoginRequest` — `@NotBlank` email/password, `@Email` on email.
- `AddressRequest` — `@NotBlank` + `@Size` on all required fields; optional `streetLine2`.
- `AddToCartRequest` — `@NotNull` bookId, `@NotNull`/`@Min(1)` quantity.
- `UpdateCartItemRequest` — `@NotNull`/`@Min(1)` quantity.
- `PlaceOrderRequest` — `@NotNull` addressId and paymentMethod (enum).

Controllers apply `@Valid`; failures produce `400 VALIDATION_ERROR` with field detail messages.

---

## 5. Contract Alignment (`openapi.yaml`)

Every path in the approved contract is implemented, and no endpoints were added:

| Method & Path | Controller | Success |
|---|---|---|
| POST `/api/auth/login` | AuthController | 200 |
| GET `/api/categories` | CategoryController | 200 |
| GET `/api/books` | BookController | 200 |
| GET `/api/books/{bookId}` | BookController | 200 |
| GET `/api/cart` | CartController | 200 |
| POST `/api/cart/items` | CartController | 200 |
| PUT `/api/cart/items/{itemId}` | CartController | 200 |
| DELETE `/api/cart/items/{itemId}` | CartController | 200 |
| GET `/api/addresses` | AddressController | 200 |
| POST `/api/addresses` | AddressController | 201 |
| POST `/api/orders` | OrderController | 201 |
| GET `/api/orders` | OrderController | 200 |
| GET `/api/orders/{orderId}` | OrderController | 200 |
| POST `/api/orders/{orderId}/cancel` | OrderController | 200 |

Secondary-scope endpoints (self-registration, address update/delete, Buy Again,
recommendations, gift points, wishlist, reviews, related products) were intentionally
**not** implemented, matching `03-api-design.md` §8.

---

## 6. Build Result

- Toolchain used: **Temurin JDK 21** + **Maven 3.10** (installed locally for verification).
- `mvn -DskipTests package` → **BUILD SUCCESS**; produced
  `target/ebookstore-1.0.0-SNAPSHOT.jar`. No compile errors.
- Tests were not run: the project has no test sources yet, and generating tests is
  Phase 6 work (and not requested for this recovery). The `target/` output was removed
  after verification; `.gitignore` already excludes `target/` and local properties.

---

## 7. Deviations from Approved Design

None. The implementation follows the approved requirements, data model, API design,
and OpenAPI contract. No new endpoints, entities, dependencies, or architecture were introduced.

---

## 8. Remaining Work (out of Phase 4 scope)

- **Database configuration & local run (Phase 5).** `spring.jpa.hibernate.ddl-auto=validate`
  requires the PostgreSQL schema to already exist. The DDL script, demo-data seeding, and the
  demo customer account are Phase 5 deliverables and are **not** created here.
- **Tests (Phase 6).** No unit/service/integration/API tests exist yet.

Neither item blocks compilation; both are expected next phases.

---

**Status: Implementation Complete — Awaiting Database Configuration and Local Run**
